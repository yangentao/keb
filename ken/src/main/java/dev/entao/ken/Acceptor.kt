package dev.entao.ken

interface Acceptor {
	fun accept(context: HttpContext, router: Router): Boolean
}

interface PermAcceptor {
	fun prepare(context: HttpContext)
	fun accept(context: HttpContext, uri: String): Boolean
}

object MethodAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		if (router.methods.isNotEmpty()) {
			if (context.request.method.toUpperCase() !in router.methods) {
				context.abort(400, "Method Error")
				return false
			}
		}
		return true
	}

}



