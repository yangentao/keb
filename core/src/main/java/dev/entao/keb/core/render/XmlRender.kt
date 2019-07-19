package dev.entao.keb.core.render

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.toXml
import dev.entao.keb.core.HttpContext
import org.w3c.dom.Element

class XmlRender(val context: HttpContext) {

	init {
		context.response.contentType = Mimes.XML
	}

	fun write(xml: String) {
		context.response.writer.print(xml)
	}

	fun write(ele: Element) {
		write(ele.toXml(false))
	}
}