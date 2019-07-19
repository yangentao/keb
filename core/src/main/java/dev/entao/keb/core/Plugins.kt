@file:Suppress("unused")

package dev.entao.keb.core

interface PermAcceptor {
	fun prepare(context: HttpContext)
	fun accept(context: HttpContext, uri: String): Boolean
}

interface HttpTimer {
	fun onHour(h: Int) {}
	fun onMinute(m: Int) {}
}

object MethodAcceptor : HttpSlice {
	override fun beforeService(context: HttpContext, router: Router): Boolean {
		if (router.methods.isNotEmpty()) {
			if (context.request.method.toUpperCase() !in router.methods) {
				context.abort(400, "Method Error")
				return false
			}
		}
		return true
	}

}
