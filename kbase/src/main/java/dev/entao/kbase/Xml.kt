package dev.entao.kbase

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Created by entaoyang@163.com on 2016/12/20.
 */



fun XmlParse(s: String): Document? {
    val fac = DocumentBuilderFactory.newInstance()
    val db = fac.newDocumentBuilder()
    val stream = ByteArrayInputStream(s.toByteArray())
    return db.parse(stream)
}

fun XmlBuild(name: String): Element {
    return XmlRoot(name)
}

fun XmlRoot(name: String): Element {
    val fac = DocumentBuilderFactory.newInstance()
    val db = fac.newDocumentBuilder()
    val doc = db.newDocument()
    val e = doc.createElement(name)
    doc.appendChild(e)
    return e
}

val Document.rootElement: Element? get() = this.firstChild as? Element

fun Node.toXml(withXmlDeclaration: Boolean): String {
    val t = TransformerFactory.newInstance().newTransformer()
    if (!withXmlDeclaration) {
        t.omitXmlDeclaration()
    }
    val w = StringWriter(512)
    t.transform(DOMSource(this), StreamResult(w))
    return w.toString()
}

fun Transformer.indent() {
    this.setOutputProperty(OutputKeys.INDENT, "yes")
}

fun Transformer.omitXmlDeclaration() {
    this.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
}


fun Element.attr(name: String): String? {
    return this.getAttribute(name)
}

fun Element.attr(name: String, value: String): Element {
    this.setAttribute(name, value)
    return this
}

fun Element.text(): String? {
    return this.textContent?.trim()
}

var Element.text: String?
    get() {
        return this.textContent?.trim()
    }
    set(value) {
        this.textContent = value
    }

val Element.textDateTime: Long
    get() {
        return this.textDateTime()
    }


//yyyy-MM-dd HH:mm:ss
fun Element.textDateTime(): Long {
    return MyDate.parseDateTime(text())?.time ?: 0L
}

fun Element.text(text: String) {
    this.addText(text)
}

fun Element.addText(text: String) {
    val node = this.ownerDocument.createTextNode(text)
    this.appendChild(node)
}

fun Element.eleText(name: String, text: String) {
    element(name) {
        textContent = text
    }
}

fun Element.element(name: String, block: Element.() -> Unit): Element {
    val e = this.ownerDocument.createElement(name)
    this.appendChild(e)
    e.block()
    return e
}


fun Element.element(name: String): Element? {
    return this.getElementsByTagName(name)?.item(0) as? Element
}

fun Element.elements(name: String): List<Element> {
    val es = ArrayList<Element>(16)
    val ls = this.getElementsByTagName(name)
    if (ls != null) {
        for (i in 0..ls.length - 1) {
            val e = ls.item(i) as Element
            es.add(e)
        }
    }
    return es
}

fun Element.elist(name: String): List<Element> {
    return this.elements(name)
}

val Element.childElements: List<Element>
    get() {
        val ls = ArrayList<Element>()
        val nodes = this.childNodes
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node is Element) {
                ls += node
            }
        }
        return ls
    }


fun escapeXML(s: String): String {
    return s.replaceChars('<' to "&lt;", '>' to "&gt;", '&' to "&amp;", '"' to "&quot;", '\'' to "&apos;")
}