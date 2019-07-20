package dev.entao.keb.core

import dev.entao.kava.log.fatal
import dev.entao.kava.log.logd
import java.util.ArrayList
import javax.servlet.FilterConfig
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface HttpSlice {
	fun onConfig(filter: HttpFilter, config: FilterConfig) {}
	fun beforeAll(context: HttpContext) {}
	fun afterAll(context: HttpContext) {}
	fun beforeService(context: HttpContext, router: Router): Boolean {
		return true
	}

	fun afterService(context: HttpContext) {}
	fun onDestory() {}
}

typealias HttpAction = KFunction<*>

class HttpActionManager {
	val allGroups = ArrayList<KClass<out HttpGroup>>()
	val routeMap = HashMap<String, Router>(32)
	private lateinit var filter: HttpFilter

	fun onConfig(filter: HttpFilter, config: FilterConfig) {
		this.filter = filter
	}

	fun onDestory() {
		routeMap.clear()
		allGroups.clear()
	}

	fun find(context: HttpContext): Router? {
		return routeMap[context.currentUri]
	}

	fun addGroup(vararg clses: KClass<out HttpGroup>) {
		allGroups.addAll(clses)
		clses.forEach { cls ->
			for (f in cls.actionList) {
				val uri = filter.actionUri(f)
				val info = Router(uri, f)
				addRouter(info)
			}
		}
	}

	fun addRouter(router: Router) {
		val u = router.uri.toLowerCase()
		if (routeMap.containsKey(u)) {
			val old = routeMap[u]
			logd("AddRouter: ", u)
			fatal("已经存在对应的Route: ${old?.function} ", u, old.toString())
		}
		routeMap[u] = router
		logd("Add Router: ", u)
	}

}