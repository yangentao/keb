@file:Suppress("unused")

package dev.entao.ken

import dev.entao.kage.DialogBuild
import dev.entao.kage.HtmlDoc
import dev.entao.kage.Tag
import dev.entao.kage.p
import dev.entao.kage.widget.button
import dev.entao.kbase.Prop1
import dev.entao.ken.anno.NavItem
import dev.entao.sql.sqlFullName
import dev.entao.kage.P
import dev.entao.kbase.isTypeInt
import dev.entao.kbase.isTypeLong
import dev.entao.kbase.isTypeString
import dev.entao.ken.ex.fromRequest
import dev.entao.sql.*
import yet.servlet.actionListWithNavItem
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2016/12/19.
 */

open class HttpPage(val context: HttpContext) {

	val request: HttpServletRequest get() = context.request
	val response: HttpServletResponse get() = context.response
	val filter: HttpFilter get() = context.filter

	val jsonSender: JsonSender get() = context.jsonSender
	val xmlSender: XmlSender get() = context.xmlSender
	val htmlSender: HtmlSender get() = context.htmlSender
	val resultSender: ResultSender get() = context.resultSender
	val fileSender: FileSender get() = context.fileSender

	val httpParams: HttpParams get() = context.httpParams
	val path: WebPath get() = context.path

	val WebPath.fullUrl: String
		get() {
			return this.fullUrlOf(request)
		}

	//label to uri
	val actionItems: List<KFunction<*>> by lazy {
		this@HttpPage::class.actionListWithNavItem.sortedBy {
			it.findAnnotation<NavItem>()?.order ?: 0
		}
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

	fun formDialog(title: String, block: (Tag) -> Unit) {
		val d = DialogBuild(context)
		d.modal.outputScript = true
		d.title(title)
		d.bodyBlock = {
			block(it)
		}
		d.closeText = "取消"
		d.buttonsBlock = {
			it.button {
				+"提交"
				classList += "m-1"
				btnPrimary()
				onclick = "yet.submitDialogPanel(this);"
			}
		}
		d.build()
		htmlSender.text(d.modal.toString())
	}

	fun formDialogDisplay(title: String, block: (Tag) -> Unit) {
		val d = DialogBuild(context)
		d.modal.outputScript = true
		d.title(title)
		d.bodyBlock = {
			block(it)
		}
		d.closeText = "确定"
		d.build()
		val s = d.modal.toString()
		htmlSender.text(s)
	}

	fun formError(title: String, msg: String) {
		val d = DialogBuild(context)
		d.modal.outputScript = true
		d.title(title)
		d.bodyBlock = {
			it.p {
				textEscaped(msg)
			}
		}
		d.closeText = "确定"
		d.build()
		val s = d.modal.toString()
		htmlSender.text(s)
	}

	fun html(block: HtmlDoc.() -> Unit) {
		val h = HtmlDoc(context)
		h.block()
		h.body.filterDeep { it.tagName == "a" || it.tagName == "button" }.forEach {
			val s = if (it.href.isNotEmpty()) {
				it.href
			} else if (it.dataUrl.isNotEmpty()) {
				it.dataUrl
			} else {
				""
			}
			if (s.isNotEmpty()) {
				if (!context.allow(s)) {
					it.addClass("d-none")
				}
			}
		}

		htmlSender.print(h)
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

	fun SQLQuery.limitPage() {
		val n = context.httpParams.int(P.pageN) ?: 0
		this.limit(P.pageSize, n * P.pageSize)
	}
}