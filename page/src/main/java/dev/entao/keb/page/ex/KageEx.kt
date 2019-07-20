package dev.entao.keb.page.ex

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.page.html.Tag

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

open class Html(httpContext: HttpContext) : Tag(httpContext, "html")