package dev.entao.keb.page.html

import dev.entao.kava.base.escapeHtml
import dev.entao.kava.base.trimColumns
import dev.entao.keb.core.HttpContext
import dev.entao.keb.page.ident

class LabelLink(var label: String, var link: String)

open class HtmlDoc(httpContext: HttpContext) : Tag(httpContext, "html") {
	val comments = ArrayList<String>()
	init {
		this.head{}
		this.body{}
	}

	fun comment(block: () -> String) {
		comments.add(block())
	}

	override fun writeTo(buf: Appendable, level: Int) {
		buf.appendln("<!DOCTYPE html>")
		comments.forEach {
			buf.appendln(it)
		}
		super.writeTo(buf, level)
	}
}

class ScriptBlock(httpContext: HttpContext, s: String) : Tag(httpContext, "script") {
	init {
		textUnsafe(s)
	}

	override fun writeChildren(singleLine: Boolean, buf: Appendable, level: Int) {
		val textTag = this.children.first() as TextUnsafe
		val s = textTag.text.trimColumns().trim()
		val lines = s.lines()
		for (line in lines) {
			buf.appendln()
			ident(buf, level + 1)
			buf.append(line)
		}
	}
}

class TextEscaped(httpContext: HttpContext, var text: String = "") : Tag(httpContext, "__textescaped__") {
	var forView = false

	override fun writeTo(buf: Appendable, level: Int) {
		buf.append(text.escapeHtml(forView))
	}

	override fun preferBufferSize(): Int {
		return text.length + 16
	}
}

class TextUnsafe(httpContext: HttpContext, var text: String = "") : Tag(httpContext, "__textunsafe__") {


	override fun writeTo(buf: Appendable, level: Int) {
		buf.append(text)
	}

	override fun preferBufferSize(): Int {
		return text.length + 16
	}
}