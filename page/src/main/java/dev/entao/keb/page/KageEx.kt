package dev.entao.keb.page

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.Keb
import dev.entao.keb.page.tag.Tag
import dev.entao.keb.page.widget.alertError
import dev.entao.keb.page.widget.alertSuccess

interface HtmlTemplate {

	fun toHtml(): String
}

fun <T : HtmlTemplate> HttpContext.writeHtml(value: T, block: T.() -> Unit) {
	block(value)
	this.writeHtml(value.toHtml())
}

fun <T : HtmlTemplate> HttpGroup.writeHtml(value: T, block: T.() -> Unit) {
	this.context.writeHtml(value, block)
}

fun <T : HtmlTemplate> HttpGroup.writeHtml(value: T) {
	this.context.writeHtml(value.toHtml())
}

class LinkItem(val label: String, val url: String, var active: Boolean = false) {
	val children: ArrayList<LinkItem> = ArrayList()
}



fun Tag.showMessagesIfPresent() {
	val s = this.httpContext.httpParams.str(Keb.ERROR) ?: ""
	if (s.isNotEmpty()) {
		this.alertError { +s }
	}
	val ss = this.httpContext.httpParams.str(Keb.SUCCESS) ?: ""
	if (ss.isNotEmpty()) {
		this.alertSuccess { +ss }
	}
}