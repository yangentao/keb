package dev.entao.keb.core

import dev.entao.kava.log.fatal
import dev.entao.kava.log.logd
import java.util.ArrayList
import javax.servlet.FilterConfig
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface HttpSlice {
	fun onConfig(filter: HttpFilter, config: FilterConfig) {}
	fun beforeAll(c: HttpContext) {}
	fun afterAll(c: HttpContext) {}
	fun beforeService(c: HttpContext, r: Router): Boolean {
		return true
	}

	fun afterService(c: HttpContext) {}
	fun onDestory() {}
}

class RouteManager {
	private var baseUri: String = ""

	val allPages = ArrayList<KClass<out HttpPage>>()
	private val map = HashMap<String, Router>(32)

	fun onConfig(filter: HttpFilter, config: FilterConfig) {
		baseUri = config.servletContext.contextPath
	}

	fun onDestory() {
		map.clear()
		allPages.clear()
	}

	fun find(c: HttpContext): Router? {
		val uri = c.request.requestURI.trimEnd('/').toLowerCase()
		return map[uri]
	}

	fun addPages(vararg clses: KClass<out HttpPage>) {
		allPages.addAll(clses)
		clses.forEach { cls ->
			for (f in cls.actionList) {
				val info = Router(WebPath.buildPath(baseUri, cls.pageName, f.actionName), f)
				addRouter(info)
			}
		}
	}

	fun addRouter(router: Router) {
		val u = router.uri.toLowerCase()
		if (map.containsKey(u)) {
			val old = map[u]
			logd("AddRouter: ", u)
			fatal("已经存在对应的Route: ${old?.function} ", u, old.toString())
		}
		map[u] = router
		logd("Add Router: ", u)
	}

}