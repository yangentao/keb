package dev.entao.ken

import dev.entao.ken.ex.contentTypePlain
import java.io.PrintWriter

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

@Suppress("unused")
open class TextSender(val context: HttpContext) {
	val writer: PrintWriter by lazy { context.response.writer }

	init {
		context.response.contentTypePlain()
	}

	fun print(s: String) {
		writer.print(s)
	}

	fun println(s: String) {
		writer.println(s)
	}

	fun text(s: String) {
		print(s)
	}



}