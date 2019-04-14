package dev.entao.ken

import dev.entao.kbase.userLabel
import dev.entao.ken.anno.NavItem
import dev.entao.yog.*
import dev.entao.sql.ConnLook
import dev.entao.kbase.MyDate
import dev.entao.ken.ex.model.Ip
import java.io.File
import java.util.*
import javax.servlet.FilterConfig
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2016/12/21.
 */

abstract class HttpFilter : BaseFilter() {

	var webConfig = WebConfig()
		private set
	val allPages = ArrayList<KClass<out HttpPage>>()

	private val map = HashMap<String, Router>(32)
	private val acceptList = ArrayList<Acceptor>(8)
	private var timer: Timer? = null
	private val timerList = ArrayList<HttpTimer>()

	var contextPath: String = ""
		private set
	// /* => "" , /person/*  => person     @WebFilter中的urlPatterns
	var patternPath: String = ""
		private set

	private val webRootFile: File by lazy {
		File(filterConfig.servletContext.getRealPath("/"))
	}
	private val webParentFile: File get() = webRootFile.parentFile

	val baseDir: File by lazy {
		File(webParentFile, contextPath.trim('/') + "_base").apply {
			if (!this.exists()) {
				this.mkdir()
			}
		}
	}

	val uploadDir: File by lazy {
		File(webParentFile, contextPath.trim('/') + "_files").apply {
			if (!exists()) {
				mkdir()
			}
		}
	}
	val tmpDir: File by lazy {
		File(webParentFile, contextPath.trim('/') + "_tmp").apply {
			if (!exists()) {
				mkdir()
			}
		}
	}

	val logDir: File by lazy {
		File(webParentFile, contextPath.trim('/') + "_log").apply {
			if (!exists()) {
				mkdir()
			}
		}
	}

	abstract fun onInit()
	open fun onDestroy() {
		ConnLook.removeThreadLocal()
	}

	//hour [0-23]
	open fun onHour(hour: Int) {
	}

	//m一直自增 , 会大于60
	open fun onMinute(m: Int) {
	}

	override fun init(filterConfig: FilterConfig) {
		super.init(filterConfig)
		contextPath = formatPatternPath(filterConfig.servletContext.contextPath)
		val pat = this::class.findAnnotation<WebFilter>()?.urlPatterns?.toList()?.firstOrNull()
				?: throw IllegalArgumentException("urlPatterns只能设置一条, 比如: /* 或 /person/*")
		patternPath = formatPatternPath(pat)

		Yog.setPrinter(YogDir(logDir, 15))
		logd("Server Start!")

		addRouterOfThis()

		addAcceptor(MethodAcceptor)
		addAcceptor(AuthWebAcceptor)
		addAcceptor(AuthAppAcceptor)
		addAcceptor(IpAcceptor)
		addAcceptor(ResAcceptor)

		addTimer(TableLimitTimer(Ip::class))
		try {
			onInit()
		} catch (ex: Exception) {
			ex.printStackTrace()
		} finally {
			ConnLook.removeThreadLocal()
		}
		timer?.cancel()
		timer = null
		val tm = Timer("everyMinute", true)
		val delay: Long = 1000 * 60
		tm.scheduleAtFixedRate(tmtask, delay, delay)
		timer = tm
	}

	private fun addRouterOfThis() {
		val ls = this::class.actionList
		for (f in ls) {
			val info = Router(WebPath.buildPath(contextPath, patternPath, f.actionName), f, this)
			addRouter(info)
		}
	}

	override fun doHttpFilter(request: HttpServletRequest, response: HttpServletResponse): Boolean {
//		logd("requestURI: ", request.requestURI)
//		request.dumpParam()
		val r = map[request.requestURI.trimEnd('/').toLowerCase()] ?: return false
		try {
			val c = HttpContext(this, request, response)
			for (a in this.acceptList) {
				try {
					if (!a.accept(c, r)) {
						return true
					}
				} catch (ex: Exception) {
					loge(ex)
				}
			}
			r.dispatch(c)
		} finally {
			ConnLook.removeThreadLocal()
		}
		return true
	}

	fun addAcceptor(a: Acceptor) {
		if (a !in this.acceptList) {
			this.acceptList += a
		}
	}

	fun addTimer(t: HttpTimer) {
		if (t !in this.timerList) {
			this.timerList += t
		}
	}

	fun addPages(vararg clses: KClass<out HttpPage>) {
		allPages.addAll(clses)
		clses.forEach { cls ->
			for (f in cls.actionList) {
				val info = Router(WebPath.buildPath(contextPath, patternPath, cls.pageName, f.actionName), f)
				addRouter(info)
			}
		}
	}

	private fun addRouter(router: Router) {
		val u = router.uri.toLowerCase()
		if (map.containsKey(u)) {
			val old = map[u]
			logd("AddRouter: ", u)
			fatal("已经存在对应的Route: ${old?.function} ", u, old.toString())
		}
		map[u] = router
		logd("Add Router: ", u)
	}

	override fun destroy() {
		timer?.cancel()
		timer = null
		map.clear()
		allPages.clear()
		acceptList.clear()
		timerList.clear()
		onDestroy()
		super.destroy()
		Yog.flush()
	}

	val navControlerList: List<Pair<String, KClass<*>>> by lazy {
		val navConList = ArrayList<Pair<String, KClass<*>>>()
		for (c in allPages) {
			val ni = c.findAnnotation<NavItem>()
			if (ni != null) {
				val lb = if (ni.group.isNotEmpty()) {
					ni.group
				} else {
					c.userLabel
				}
				navConList.add(lb to c)
			}
		}
		navConList
	}
	private val tmtask = object : TimerTask() {

		private var minN: Int = 0
		private var preHour = -1

		override fun run() {
			val timers = ArrayList<HttpTimer>(this@HttpFilter.timerList)
			try {
				val h = MyDate().hour
				if (h != preHour) {
					preHour = h
					try {
						onHour(h)
					} catch (ex: Exception) {
						loge(ex)
					}
					for (ht in timers) {
						try {
							ht.onHour(h)
						} catch (ex: Exception) {
							loge(ex)
						}
					}
				}
			} catch (ex: Exception) {
				loge(ex)
			}
			try {
				val n = minN++
				try {
					onMinute(n)
				} catch (ex: Exception) {
					loge(ex)
				}
				for (mt in timers) {
					try {
						mt.onMinute(n)
					} catch (ex: Exception) {
						loge(ex)
					}
				}
			} catch (ex: Exception) {
				loge(ex)
			}
			try {
				Yog.flush()
			} catch (ex: Exception) {

			}
			ConnLook.removeThreadLocal()
		}
	}

	companion object {
		const val PAGE = "Page"
		const val API_PAGE = "Api"
		const val ACTION = "Action"
		const val INDEX = "index"

	}
}