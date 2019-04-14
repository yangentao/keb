@file:Suppress("unused")

package dev.entao.ken

import java.io.PrintWriter

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

open class HtmlSender(val context: HttpContext) {

	private val writer: PrintWriter by lazy { context.response.writer }

	init {
		context.response.contentTypeHtml()
	}

	fun print(s: String) {
		writer.print(s)
	}

	fun println(s: String) {
		writer.println(s)
	}

	fun text(s: String) {
		this.print(s)
	}

}