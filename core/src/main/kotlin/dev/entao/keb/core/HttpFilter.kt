@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.keb.core

import dev.entao.kava.base.hasAnnotation
import dev.entao.kava.base.ownerClass
import dev.entao.kava.log.Yog
import dev.entao.kava.log.YogDir
import dev.entao.kava.log.logd
import java.util.*
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2016/12/21.
 */
//    @WebFilter中的urlPatterns =  "/*"
abstract class HttpFilter : Filter {

	var sessionTimeoutSeconds: Int = 3600

	var loginUri: String = ""

	var webConfig = WebConfig()
		private set

	private lateinit var filterConfig: FilterConfig
	var contextPath: String = ""
		private set
	// /* => "" , /person/*  => person     @WebFilter中的urlPatterns
	var patternPath: String = ""
		private set

	val webDir = WebDir()
	val routeManager = HttpActionManager()

	private val sliceList = ArrayList<HttpSlice>()
	val timerSlice = TimerSlice()

	val allGroups: ArrayList<KClass<out HttpGroup>> get() = routeManager.allGroups

	val infoMap = HashMap<String, Any>()

	fun addSlice(hs: HttpSlice) {
		sliceList += hs
	}

	abstract fun onInit()

	abstract fun cleanThreadLocals()

	open fun onDestroy() {

	}

	final override fun init(filterConfig: FilterConfig) {
		this.filterConfig = filterConfig
		sliceList.clear()
		contextPath = filterConfig.servletContext.contextPath
		val pat = this::class.findAnnotation<WebFilter>()?.urlPatterns?.toList()?.firstOrNull()
				?: throw IllegalArgumentException("urlPatterns只能设置一条, 比如: /* 或 /person/*")
		patternPath = pat.filter { it.isLetterOrDigit() || it == '_' }

		webDir.onConfig(this, filterConfig)
		Yog.setPrinter(YogDir(webDir.logDir, 15))
		logd("Server Start!")

		routeManager.onConfig(this, filterConfig)
		addRouterOfThis()

		addSlice(timerSlice)
		addSlice(MethodAcceptor)
		addSlice(LoginCheckSlice)

		try {
			onInit()
			for (hs in sliceList) {
				hs.onConfig(this, filterConfig)
			}
			if (this.loginUri.isEmpty()) {
				for ((k, v) in routeManager.routeMap) {
					if (v.function.hasAnnotation<LoginAction>()) {
						this.loginUri = k
						break
					}
				}
			}

		} catch (ex: Exception) {
			ex.printStackTrace()
		} finally {
			cleanThreadLocals()
		}
	}

	final override fun destroy() {
		routeManager.onDestory()
		for (hs in sliceList) {
			hs.onDestory()
		}
		onDestroy()
		Yog.flush()
		cleanThreadLocals()
	}

	fun resUri(res: String): String {
		return buildPath(contextPath, res)
	}

	fun actionUri(ac: KFunction<*>): String {
		val cls = ac.ownerClass!!
		if (cls == this::class) {
			return buildPath(contextPath, patternPath, ac.actionName)
		}
		return buildPath(contextPath, patternPath, cls.pageName, ac.actionName)
	}

	fun groupUri(g: KClass<*>): String {
		return buildPath(contextPath, patternPath, g.pageName)
	}

	private fun addRouterOfThis() {
		val ls = this::class.actionList
		for (f in ls) {
			val uri = actionUri(f)
			val info = Router(uri, f, this)
			routeManager.addRouter(info)
		}
	}

	fun addGroup(vararg clses: KClass<out HttpGroup>) {
		this.routeManager.addGroup(*clses)
	}

	final override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
		try {
			if (request is HttpServletRequest && response is HttpServletResponse) {
				request.characterEncoding = "UTF-8"
				response.characterEncoding = "UTF-8"
				val c = HttpContext(this, request, response, chain)
				for (hs in sliceList) {
					hs.beforeAll(c)
				}
				val r = routeManager.find(c)
				if (r == null) {
					chain.doFilter(request, response)
				} else {
					doHttpFilter(c, r)
				}
				for (hs in sliceList) {
					hs.afterAll(c)
				}
			} else {
				chain.doFilter(request, response)
			}
		} catch (ex: Exception) {
			logd(ex)
			throw  ex
		} finally {
			cleanThreadLocals()
		}
	}

	fun doHttpFilter(c: HttpContext, r: Router) {
		for (hs in sliceList) {
			if (!hs.beforeService(c, r)) {
				return
			}
		}
		r.dispatch(c)
		for (hs in sliceList) {
			hs.afterService(c)
		}
	}

	//hour [0-23]
	open fun onHour(hour: Int) {
	}

	//m一直自增 , 会大于60
	open fun onMinute(m: Int) {
	}

	val navControlerList: List<Pair<String, KClass<*>>> by lazy {
		val navConList = ArrayList<Pair<String, KClass<*>>>()
//		for (c in allPages) {
//			val ni = c.findAnnotation<NavItem>()
//			if (ni != null) {
//				val lb = if (ni.group.isNotEmpty()) {
//					ni.group
//				} else {
//					c.userLabel
//				}
//				navConList.add(lb to c)
//			}
//		}
		navConList
	}

	companion object {
		const val GROUP_SUFFIX = "Group"
		const val ACTION = "Action"
		const val INDEX = "index"

	}
}