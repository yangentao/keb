@file:Suppress("unused", "FunctionName")

package dev.entao.keb.core

import dev.entao.kava.base.Prop1
import dev.entao.kava.base.isTypeInt
import dev.entao.kava.base.isTypeLong
import dev.entao.kava.base.isTypeString
import dev.entao.keb.core.render.ResultRender
import kotlin.reflect.KFunction

/**
 * Created by entaoyang@163.com on 2016/12/19.
 */

open class HttpGroup(final override val context: HttpContext) : HttpScope {

	val resultSender: ResultRender by lazy {
		ResultRender(context)
	}

	open fun indexAction() {
		context.abort(404)
	}
}

interface HttpScope {
	val context: HttpContext

	val httpParams: HttpParams get() = context.httpParams

	fun HttpAction.param(value: Any): UriMake {
		return UriMake(context, this).param(value)
	}

	fun HttpAction.arg(key: String, value: Any): UriMake {
		return UriMake(context, this).arg(key, value)
	}

	val HttpAction.uri: String
		get() {
			return context.actionUri(this)
		}

	fun resUri(file: String): String {
		return context.resUri(file)
	}

	fun redirect(action: KFunction<*>) {
		context.redirect(context.actionUri(action))
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