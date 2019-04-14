package dev.entao.ken

import dev.entao.ken.ex.contentTypeHtml
import dev.entao.kage.HtmlDoc
import dev.entao.kage.Tag
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

	fun tag(t: Tag) {
		writer.print(t.toString())
	}

	fun text(s: String) {
		print(s)
	}

	fun print(html: HtmlDoc) {
		print(html.toString())
	}

}