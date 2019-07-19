@file:Suppress("unused", "FunctionName")

package dev.entao.keb.core

import dev.entao.kava.base.Prop1
import dev.entao.kava.base.isTypeInt
import dev.entao.kava.base.isTypeLong
import dev.entao.kava.base.isTypeString
import dev.entao.kava.sql.*
import dev.entao.keb.core.render.FileSender
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KFunction

/**
 * Created by entaoyang@163.com on 2016/12/19.
 */

open class HttpGroup(val context: HttpContext) {


	val httpParams: HttpParams get() = context.httpParams
	val path: WebPath get() = context.path

	val WebPath.fullUrl: String
		get() {
			return this.fullUrlOf(context.request)
		}

	fun resUri(file: String): String {
		return path.uriRes(file)
	}


	fun redirect(action: KFunction<*>, param: Any?) {
		redirect(action) {
			param(param)
		}
	}

	fun redirect(action: KFunction<*>) {
		context.redirect(action)
	}

	fun redirect(action: KFunction<*>, block: WebPath.() -> Unit) {
		val p = WebPath(context.filter, action)
		p.block()
		context.redirect(p)
	}

	fun redirect(p: WebPath) {
		context.redirect(p)
	}

	fun path(action: KFunction<*>): WebPath {
		return path.action(action)
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