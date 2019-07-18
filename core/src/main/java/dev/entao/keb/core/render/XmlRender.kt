package dev.entao.keb.core.render

import dev.entao.kava.base.toXml
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.contentTypeXml
import org.w3c.dom.Element

class XmlRender(val context: HttpContext) {

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