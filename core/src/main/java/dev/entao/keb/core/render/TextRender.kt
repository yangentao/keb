package dev.entao.keb.core.render

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.contentTypePlain
import java.io.PrintWriter

@Suppress("unused")
open class TextRender(val context: HttpContext) {

	private val writer: PrintWriter by lazy { context.response.writer }

	init {
		context.response.contentTypePlain()
	}

	fun writeln(s: String) {
		writer.println(s)
	}

	fun write(s: String) {
		print(s)
	}
}
