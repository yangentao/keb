package dev.entao.keb.core

import dev.entao.kava.base.Prop1
import dev.entao.kava.base.isTypeInt
import dev.entao.kava.base.isTypeLong
import dev.entao.kava.base.isTypeString


interface HttpScope {
	val context: HttpContext
	val httpParams: HttpParams get() = context.httpParams

	val HttpAction.uri: String
		get() {
			return context.filter.actionUri(this)
		}

	fun redirect(action: HttpAction) {
		context.redirect(action.uri)
	}

	fun redirect(actionUrl: ActionURL) {
		context.redirect(actionUrl.toURL(context))
	}


	fun resUri(file: String): String {
		return context.resUri(file)
	}


	fun paramValue(p: Prop1): Any? {
		if (p.isTypeInt) {
			return context.httpParams.int(p)
		}
		if (p.isTypeLong) {
			return context.httpParams.long(p)
		}
		if (p.isTypeString) {
			return context.httpParams.str(p)
		}
		return null
	}

}