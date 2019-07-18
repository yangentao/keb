@file:Suppress("unused", "FunctionName")

package dev.entao.keb.core

import dev.entao.kava.base.Prop1
import dev.entao.kava.base.isTypeInt
import dev.entao.kava.base.isTypeLong
import dev.entao.kava.base.isTypeString
import dev.entao.kava.sql.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2016/12/19.
 */

open class HttpGroup(val context: HttpContext) {

	val request: HttpServletRequest get() = context.request
	val response: HttpServletResponse get() = context.response
	val filter: HttpFilter get() = context.filter

	val htmlSender: HtmlSender get() = context.htmlSender
	val jsonSender: JsonSender get() = context.jsonSender
	val xmlSender: XmlSender get() = context.xmlSender
	val resultSender: ResultSender get() = context.resultSender
	val fileSender: FileSender get() = context.fileSender

	val httpParams: HttpParams get() = context.httpParams
	val path: WebPath get() = context.path

	val WebPath.fullUrl: String
		get() {
			return this.fullUrlOf(request)
		}



	fun resUri(file: String): String {
		return path.uriRes(file)
	}

	fun Model.fromRequest() {
		this.fromRequest(request)
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
		val p = WebPath(filter, action)
		p.block()
		context.redirect(p)
	}

	fun redirect(p: WebPath) {
		context.redirect(p)
	}

	fun path(action: KFunction<*>): WebPath {
		return path.action(action)
	}


	private fun paramValue(p: Prop1): Any? {
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

	fun EQ(vararg ps: Prop1): Where? {
		var w: Where? = null
		for (p in ps) {
			val v = paramValue(p) ?: continue
			w = w AND p.sqlFullName.EQ(v)
		}
		return w
	}

	// %value%
	fun LIKE(p: Prop1): Where? {
		val v = httpParams.str(p)?.trim() ?: return null
		if (v.isEmpty()) {
			return null
		}
		return p LIKE """%$v%"""
	}

	// value%
	fun LIKE_(p: Prop1): Where? {
		val v = httpParams.str(p)?.trim() ?: return null
		if (v.isEmpty()) {
			return null
		}
		return p LIKE """$v%"""
	}

	// %value
	fun _LIKE(p: Prop1): Where? {
		val v = httpParams.str(p)?.trim() ?: return null
		if (v.isEmpty()) {
			return null
		}
		return p LIKE """%$v"""
	}

	fun NE(p: Prop1): Where? {
		val v = paramValue(p) ?: return null
		return p NE v
	}

	fun GE(p: Prop1): Where? {
		val v = paramValue(p) ?: return null
		return p GE v
	}

	fun GT(p: Prop1): Where? {
		val v = paramValue(p) ?: return null
		return p GT v
	}

	fun LE(p: Prop1): Where? {
		val v = paramValue(p) ?: return null
		return p LE v
	}

	fun LT(p: Prop1): Where? {
		val v = paramValue(p) ?: return null
		return p LT v
	}


}