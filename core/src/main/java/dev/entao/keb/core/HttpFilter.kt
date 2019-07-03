@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.keb.core

import dev.entao.kava.log.Yog
import dev.entao.kava.log.YogDir
import dev.entao.kava.log.logd
import dev.entao.kava.sql.ConnLook
import java.io.File
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 * Created by entaoyang@163.com on 2016/12/21.
 */
//    @WebFilter中的urlPatterns =  "/*"
abstract class HttpFilter : Filter {

	var webConfig = WebConfig()
		private set

	private lateinit var filterConfig: FilterConfig
	var contextPath: String = ""
		private set
	val webDir = WebDir()
	val routeManager = RouteManager()

	private val sliceList = ArrayList<HttpSlice>()
	val timerSlice = TimerSlice()
	val accepterSlice = AccepterManager()

	val allPages: ArrayList<KClass<out HttpPage>> get() = routeManager.allPages

	fun addSlice(hs: HttpSlice) {
		sliceList += hs
	}

	abstract fun onInit()
	open fun onDestroy() {

	}

	final override fun init(filterConfig: FilterConfig) {
		this.filterConfig = filterConfig
		sliceList.clear()
		contextPath = filterConfig.servletContext.contextPath
		webDir.onConfig(this, filterConfig)
		Yog.setPrinter(YogDir(webDir.logDir, 15))
		logd("Server Start!")

		routeManager.onConfig(this, filterConfig)
		addRouterOfThis()

		addSlice(timerSlice)
		addSlice(accepterSlice)
		accepterSlice.addAcceptor(MethodAcceptor)

		try {
			onInit()
			for (hs in sliceList) {
				hs.onConfig(this, filterConfig)
			}
		} catch (ex: Exception) {
			ex.printStackTrace()
		} finally {
			ConnLook.removeThreadLocal()
		}
	}

	final override fun destroy() {
		routeManager.onDestory()
		for (hs in sliceList) {
			hs.onDestory()
		}
		onDestroy()
		Yog.flush()
		ConnLook.removeThreadLocal()
	}

	private fun addRouterOfThis() {
		val ls = this::class.actionList
		for (f in ls) {
			val info = Router(WebPath.buildPath(contextPath, f.actionName), f, this)
			routeManager.addRouter(info)
		}
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
			ConnLook.removeThreadLocal()
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
		const val PAGE = "Page"
		const val API_PAGE = "Api"
		const val ACTION = "Action"
		const val INDEX = "index"

	}
}