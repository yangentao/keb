@file:Suppress("unused")

package dev.entao.ken

import dev.entao.ken.ex.contentTypeXml
import org.w3c.dom.Element
import dev.entao.kbase.toXml

class XmlSender(val context: HttpContext) {

	init {
		context.response.contentTypeXml()
	}

	fun send(xml: String) {
		context.response.writer.print(xml)
	}

	fun send(ele: Element) {
		send(ele.toXml(false))
	}
}