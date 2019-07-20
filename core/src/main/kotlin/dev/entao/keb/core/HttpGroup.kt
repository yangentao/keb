@file:Suppress("unused", "FunctionName")

package dev.entao.keb.core

import dev.entao.kava.base.Prop1
import dev.entao.kava.base.isTypeInt
import dev.entao.kava.base.isTypeLong
import dev.entao.kava.base.isTypeString
import kotlin.reflect.KFunction

/**
 * Created by entaoyang@163.com on 2016/12/19.
 */

abstract class HttpGroup(context: HttpContext) : HttpScope(context) {

	abstract fun indexAction()
}

open class HttpScope(val context: HttpContext) {


	val httpParams: HttpParams get() = context.httpParams

	fun HttpAction.param(value: Any): UriMake {
		return UriMake(context, this).param(value)
	}

	fun HttpAction.arg(key: String, value: Any): UriMake {
		return UriMake(context, this).arg(key, value)
	}

	fun resUri(file: String): String {
		return context.resUri(file)
	}

	fun redirect(action: KFunction<*>, param: Any?) {
		context.redirect(context.actionUri(action, param))
	}



	fun redirect(action: KFunction<*>, block: UriMake.() -> Unit) {
		val p = UriMake(context, action)
		p.block()
		context.redirect(p.uri)
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