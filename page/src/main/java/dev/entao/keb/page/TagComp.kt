package dev.entao.keb.page

import dev.entao.kava.base.escapeHtml
import dev.entao.kava.base.trimColumns

class LabelLink(var label: String, var link: String)

open class HtmlDoc(httpContext: dev.entao.keb.core.HttpContext) : Tag(httpContext, "html") {
	val comments = ArrayList<String>()
	val head = Tag(httpContext, "head")
	val body = BodyTag(httpContext)

	init {
		addTag(head)
		addTag(body)
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

class BodyTag(httpContext: dev.entao.keb.core.HttpContext) : Tag(httpContext, "body") {

	init {
		outputScript = true
	}
}

class ScriptBlock(httpContext: dev.entao.keb.core.HttpContext, s: String) : Tag(httpContext, "script") {
	init {
		this.type = "text/javascript"
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

class ScriptLink(httpContext: dev.entao.keb.core.HttpContext, src: String) : Tag(httpContext, "script") {
	init {
		this.type = "text/javascript"
		this.src = src
	}
}

class TextEscaped(httpContext: dev.entao.keb.core.HttpContext, var text: String = "") : Tag(httpContext, "__textescaped__") {
	var forView = false

	override fun writeTo(buf: Appendable, level: Int) {
		buf.append(text.escapeHtml(forView))
	}

	override fun preferBufferSize(): Int {
		return text.length + 16
	}
}

class TextUnsafe(httpContext: dev.entao.keb.core.HttpContext, var text: String = "") : Tag(httpContext, "__textunsafe__") {


	override fun writeTo(buf: Appendable, level: Int) {
		buf.append(text)
	}

	override fun preferBufferSize(): Int {
		return text.length + 16
	}
}