package dev.entao.keb.core.render

import dev.entao.kava.base.Mimes
import dev.entao.keb.core.HttpContext
import java.io.PrintWriter

@Suppress("unused")
open class TextRender(val context: HttpContext) {

	private val writer: PrintWriter by lazy { context.response.writer }

	init {
		context.response.contentType = Mimes.PLAIN
	}

	fun writeln(s: String) {
		writer.println(s)
	}

	fun write(s: String) {
		print(s)
	}
}
