package dev.entao.keb.page.tag

import dev.entao.kava.base.escapeHtml
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.contentTypeHtml
import dev.entao.keb.page.attrVal
import dev.entao.keb.page.ident
import java.util.ArrayList

class HtmlPrinter(val tag: Tag) {
	var bottomScript = tag.tagName == "html"
	var docDeclared = tag.tagName == "html"

	private val scriptList = ArrayList<Tag>()

	override fun toString(): String {
		val buffer = StringBuilder(4096)
		writeTo(buffer)
		return buffer.toString()
	}

	fun writeTo(buf: Appendable) {
		if (docDeclared) {
			buf.appendln("<!DOCTYPE HTML>")
		}
		this.writeTo(tag, buf, 0, false)
	}

	private fun allowEmptyAttr(tag: Tag, k: String): Boolean {
		if (tag.tagName == "col" && k == "width") {
			return true
		}
		if (tag.tagName == "option" && k == "value") {
			return true
		}
		return false
	}

	private fun writeAttrs(tag: Tag, buf: Appendable) {
		for ((k, v) in tag.attrs) {
			if (v.isEmpty() && !allowEmptyAttr(tag, k)) {
				continue
			}
			buf.append(" ").append(k).append("=").append(attrVal(v))
		}
	}

	private fun writeTo(tag: Tag, buf: Appendable, level: Int, singleLine: Boolean) {
		if (tag is TextUnsafe) {
			buf.append(tag.text)
			return
		}
		if (tag is TextEscaped) {
			buf.append(tag.text.escapeHtml(tag.forView))
			return
		}

		if (!singleLine) {
			buf.ident(level)
		}
		buf.append("<").append(tag.tagName)
		writeAttrs(tag, buf)
		if (tag.children.isEmpty()) {
			if (tag.tagName in selfEndTags) {
				buf.append("/>")
			} else {
				buf.append("></").append(tag.tagName).append(">")
			}
			return
		}
		buf.append(">")
		val oneLine = singleLine || tag.tagName in singleLineTags
		if (!oneLine) {
			buf.appendln()
		}
		for (c in tag.children) {
			if (bottomScript) {
				if (c.tagName == "script") {
					scriptList += c
					continue
				}
			}
			writeTo(c, buf, level + 1, oneLine)
			if (!oneLine) {
				buf.appendln()
			}

		}
		if (bottomScript) {
			if (tag.tagName == "body" || tag === this.tag) {
				for (c in this.scriptList) {
					writeTo(c, buf, 1, c["src"].isEmpty())
					buf.appendln()
				}
				scriptList.clear()
			}
		}
		if (!oneLine) {
			buf.ident(level)
		}
		buf.append("</").append(tag.tagName).append(">")

	}

	companion object {
		val selfEndTags = setOf("meta", "link", "input", "img", "hr")
		val singleLineTags = setOf("span", "textarea", "label", "button", "title", "td", "th", "input", "option", "a", "h1", "h2", "h3", "h4", "h5", "h6")
	}
}

fun HttpContext.sendHtmlTag(tag: Tag) {
	val p = HtmlPrinter(tag)
	p.bottomScript = true
	val r = this.response
	r.contentTypeHtml()
	val w = r.writer
	p.writeTo(w)
	w.flush()
}

