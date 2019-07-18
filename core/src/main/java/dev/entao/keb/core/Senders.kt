@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.core

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.substringBetween
import dev.entao.kava.base.toXml
import dev.entao.kava.json.*
import dev.entao.kava.log.logd
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter
import javax.servlet.ServletOutputStream

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


