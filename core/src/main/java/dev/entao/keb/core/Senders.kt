@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.core

import java.io.PrintWriter

open class HtmlSender(val context: HttpContext) {

	private val writer: PrintWriter by lazy { context.response.writer }

	init {
		context.response.contentTypeHtml()
	}

	fun writeln(s: String) {
		writer.println(s)
	}

	fun write(s: String) {
		writer.print(s)
	}
}


