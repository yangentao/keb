@file:Suppress("unused")

package dev.entao.keb.page.html

import dev.entao.kava.base.Prop
import dev.entao.kava.base.userLabel
import dev.entao.kava.base.userName
import dev.entao.keb.page.B

typealias TagCallback = Tag.() -> Unit


fun Tag.head(block: TagCallback): Tag {
	val h = this.headTag
	if (h == null) {
		return tag("head", block)
	} else {
		block(h)
		return h
	}
}

fun Tag.body(block: TagCallback): Tag {
	val b = this.bodyTag
	if (b == null) {
		return tag("body", block)
	} else {
		block(b)
		return b
	}
}

fun Tag.lang(language: String = "zh-CN"): Tag {
	this.lang = language
	return this
}

fun Tag.title(s: String) {
	val a = this.findChild { it.tagName == "title" }
	if (a == null) {
		val t = tag("title")
		t.textEscaped(s)
	} else {
		a.children.clear()
		a.textEscaped(s)
	}
}

fun Tag.meta(block: TagCallback): Tag {
	return tag("meta", block)
}

fun Tag.metaKeywords(vararg words: String): Tag {
	return this.metaKeywords(words.toList())
}

fun Tag.metaKeywords(set: List<String>): Tag {
	meta {
		name = "keywords"
		content = set.joinToString(",")
	}
	return this
}

fun Tag.metaCharset(charset: String = "UTF-8"): Tag {
	meta {
		this.charset = charset
	}
	return this
}

fun Tag.metaDesc(desc: String): Tag {
	meta {
		name = "description"
		content = desc
	}
	return this
}

fun Tag.link(block: TagCallback): Tag {
	return tag("link", block)
}

fun Tag.linkShortcut(href: String) {
	link {
		rel = "shortcut"
		type = "image/x-icon"
		this.href = href
	}
}

fun Tag.linkStylesheet(href: String): Tag {
	return link {
		rel = "stylesheet"
		type = "text/css"
		this.href = href
	}
}

fun Tag.style(block: () -> String) {
	if (this.tagName == "head") {
		tag("style") {
			+block()
		}
		return
	}
	this.headTag?.tag("style") {
		+block()
	}
}

fun Tag.font(size: Int, color: String, block: TagCallback) {
	tag("font") {
		this.size = size
		this.color = color
		block()
	}
}

fun Tag.label(p: Prop, forIt: Boolean): Tag {
	val t = label(p.userLabel)
	if (forIt) {
		t.forId = p.userName
	}
	return t

}

fun Tag.label(p: Prop): Tag {
	return label(p.userLabel)
}

fun Tag.label(text: String): Tag {
	return label {
		+text
	}
}

fun Tag.label(block: TagCallback): Tag {
	return tag("label", block)
}

fun Tag.script(block: TagCallback): Tag {
	return tag("script", block)
}

fun Tag.scriptLink(src: String): Tag {
	return this.script {
		this.src = src
	}
}

fun Tag.scriptBlock(block: () -> String): ScriptBlock {
	val t = ScriptBlock(this.httpContext, block())
	tag(t)
	return t
}

fun Tag.div(block: TagCallback): Tag {
	return tag("div", block)
}

fun Tag.div(cls: String, block: TagCallback): Tag {
	val t = tag("div")
	t.clazz = cls
	t.block()
	return t
}

fun Tag.span(block: TagCallback): Tag {
	return tag("span", block)
}

fun Tag.br(): Tag {
	return tag("br")
}

fun Tag.hr(): Tag {
	return this.hr {}
}

fun Tag.hr(block: TagCallback): Tag {
	return tag("hr", block)
}

fun Tag.pre(block: TagCallback): Tag {
	return tag("pre", block)
}

fun Tag.code(block: TagCallback): Tag {
	return tag("code", block)
}

fun Tag.ol(block: TagCallback): Tag {
	return tag("ol", block)
}

fun Tag.ul(block: TagCallback): Tag {
	return tag("ul", block)
}

fun Tag.li(block: TagCallback): Tag {
	return tag("li", block)
}

fun Tag.h1(block: TagCallback): Tag {
	return tag("h1", block)
}

fun Tag.h2(block: TagCallback): Tag {
	return tag("h2", block)
}

fun Tag.h3(block: TagCallback): Tag {
	return tag("h3", block)
}

fun Tag.h4(block: TagCallback): Tag {
	return tag("h4", block)
}

fun Tag.h5(block: TagCallback): Tag {
	return tag("h5", block)
}

fun Tag.h6(block: TagCallback): Tag {
	return tag("h6", block)
}

fun Tag.p(block: TagCallback): Tag {
	return tag("p", block)
}

fun Tag.dl(block: TagCallback): Tag {
	return tag("dl", block)
}

fun Tag.dt(block: TagCallback): Tag {
	return tag("dt", block)
}

fun Tag.dd(block: TagCallback): Tag {
	return tag("dd", block)
}

fun Tag.table(block: TagCallback): Tag {
	return tag("table") {
		clazz = "table"
		block()
	}
}

fun Tag.thead(block: TagCallback): Tag {
	return tag("thead", block)
}

fun Tag.tbody(block: TagCallback): Tag {
	return tag("tbody", block)
}

fun Tag.th(block: TagCallback): Tag {
	return tag("th", block)
}

fun Tag.tr(block: TagCallback): Tag {
	return tag("tr", block)
}

fun Tag.td(block: TagCallback): Tag {
	return tag("td", block)
}

fun Tag.col(block: TagCallback): Tag {
	return tag("col", block)
}

fun Tag.colgroup(block: TagCallback): Tag {
	return tag("colgroup", block)
}

fun Tag.well(block: TagCallback): Tag {
	return tag("well", block)
}

fun Tag.strong(block: TagCallback): Tag {
	return tag("strong", block)
}

fun Tag.img(block: TagCallback): Tag {
	return tag("img", block)
}

fun Tag.small(block: TagCallback): Tag {
	return tag("small", block)
}

fun Tag.nav(block: TagCallback): Tag {
	return tag("nav", block)
}

fun Tag.mutedText(block: TagCallback): Tag {
	val t = tag("small")
	t.clazz = B.textMuted
	t.block()
	return t
}

fun Tag.pArticle(text: String) {
	val textList = text.split("\n")
	for (s in textList) {
		val t = tag("p")
		t.style = "text-indent:2em"
		t.textEscaped(s).forView = true
	}
}

fun Tag.pArticle(block: () -> String) {
	pArticle(block())
}

