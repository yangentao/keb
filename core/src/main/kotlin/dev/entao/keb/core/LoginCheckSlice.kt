package dev.entao.keb.core

import dev.entao.kava.base.hasAnnotation

object LoginCheckSlice : HttpSlice {

	override fun beforeService(context: HttpContext, router: Router): Boolean {
		if (router.function.hasAnnotation<NeedLogin>() || router.cls.hasAnnotation<NeedLogin>()) {
			if (!context.isLogined) {
				if (context.filter.loginUri.isNotEmpty()) {
					if (context.acceptHtml) {
						val u = Url(context.filter.loginUri)
						u.replace(KebConst.BACK_URL, context.fullUrlOf(router.uri))
						context.redirect(u.build())
						return false
					}
				}
				context.abort(401)
				return false
			}
		}
		return true
	}
}