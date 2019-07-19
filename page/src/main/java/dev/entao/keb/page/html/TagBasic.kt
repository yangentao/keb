@file:Suppress("unused")

package dev.entao.keb.page.html

import dev.entao.kava.base.Prop
import dev.entao.kava.base.userName
import dev.entao.keb.page.B

typealias TagCallback = Tag.() -> Unit

fun Tag.head(block: TagCallback): Tag {
	return addTag("head", block)
}

fun Tag.body(block: TagCallback): Tag {
	return addTag("body", block)
}

val Tag.topMost: Tag
	get() {
		var p = this
		while (p.parentTag != null) {
			p = p.parentTag!!
		}
		return p
	}

val Tag.head: Tag
	get() {
		return this.topMost.findChildDeep { it.tagName == "head" }!!
	}

val Tag.body: Tag
	get() {
		return this.topMost.findChildDeep { it.tagName == "body" }!!
	}

fun Tag.lang(language: String = "zh-CN"): Tag {
	this.lang = language
	return this
}

fun Tag.title(s: String) {
	val t = addTag("title")
	t.textEscaped(s)
}

fun Tag.meta(block: TagCallback): Tag {
	return addTag("meta", block)
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
	return addTag("link", block)
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
		addTag("style") {
			+block()
		}
		return
	}
	this.htmlTag?.findChild { it.tagName == "head" }?.addTag("style") {
		+block()
	}
}

fun Tag.font(size: Int, color: String, block: TagCallback) {
	addTag("font") {
		this.size = size
		this.color = color
		this.block()
	}
}

fun Tag.label(p: Prop, forIt: Boolean): Tag {
	val t = label(p.userName)
	if (forIt) {
		t.forId = p.userName
	}
	return t

}

fun Tag.label(p: Prop): Tag {
	return label(p.userName)
}

fun Tag.label(text: String): Tag {
	return label {
		+text
	}
}

fun Tag.label(block: TagCallback): Tag {
	return addTag("label", block)
}

fun Tag.script(block: TagCallback): Tag {
	return addTag("script", block)
}

fun Tag.scriptLink(src: String): Tag {
	return this.script {
		this.src = src
	}
}

fun Tag.scriptBlock(block: () -> String): ScriptBlock {
	val t = ScriptBlock(this.httpContext, block())
	addTag(t)
	return t
}

fun Tag.div(block: TagCallback): Tag {
	return addTag("div", block)
}

fun Tag.div(cls: String, block: TagCallback): Tag {
	val t = addTag("div")
	t.clazz = cls
	t.block()
	return t
}

fun Tag.span(block: TagCallback): Tag {
	return addTag("span", block)
}

fun Tag.br(): Tag {
	return addTag("br")
}

fun Tag.hr(): Tag {
	return this.hr {}
}

fun Tag.hr(block: TagCallback): Tag {
	return addTag("hr", block)
}

fun Tag.pre(block: TagCallback): Tag {
	return addTag("pre", block)
}

fun Tag.code(block: TagCallback): Tag {
	return addTag("code", block)
}

fun Tag.ol(block: TagCallback): Tag {
	return addTag("ol", block)
}

fun Tag.ul(block: TagCallback): Tag {
	return addTag("ul", block)
}

fun Tag.li(block: TagCallback): Tag {
	return addTag("li", block)
}

fun Tag.h1(block: TagCallback): Tag {
	return addTag("h1", block)
}

fun Tag.h2(block: TagCallback): Tag {
	return addTag("h2", block)
}

fun Tag.h3(block: TagCallback): Tag {
	return addTag("h3", block)
}

fun Tag.h4(block: TagCallback): Tag {
	return addTag("h4", block)
}

fun Tag.h5(block: TagCallback): Tag {
	return addTag("h5", block)
}

fun Tag.h6(block: TagCallback): Tag {
	return addTag("h6", block)
}

fun Tag.p(block: TagCallback): Tag {
	return addTag("p", block)
}

fun Tag.dl(block: TagCallback): Tag {
	return addTag("dl", block)
}

fun Tag.dt(block: TagCallback): Tag {
	return addTag("dt", block)
}

fun Tag.dd(block: TagCallback): Tag {
	return addTag("dd", block)
}

fun Tag.table(block: TagCallback): Tag {
	return addTag("table") {
		clazz = "table"
		block()
	}
}

fun Tag.thead(block: TagCallback): Tag {
	return addTag("thead", block)
}

fun Tag.tbody(block: TagCallback): Tag {
	return addTag("tbody", block)
}

fun Tag.th(block: TagCallback): Tag {
	return addTag("th", block)
}

fun Tag.tr(block: TagCallback): Tag {
	return addTag("tr", block)
}

fun Tag.td(block: TagCallback): Tag {
	return addTag("td", block)
}

fun Tag.col(block: TagCallback): Tag {
	return addTag("col", block)
}

fun Tag.colgroup(block: TagCallback): Tag {
	return addTag("colgroup", block)
}

fun Tag.well(block: TagCallback): Tag {
	return addTag("well", block)
}

fun Tag.strong(block: TagCallback): Tag {
	return addTag("strong", block)
}

fun Tag.img(block: TagCallback): Tag {
	return addTag("img", block)
}

fun Tag.small(block: TagCallback): Tag {
	return addTag("small", block)
}

fun Tag.nav(block: TagCallback): Tag {
	return addTag("nav", block)
}

fun Tag.smallMuted(block: TagCallback): Tag {
	val t = addTag("small")
	t.clazz = B.textMuted
	t.block()
	return t
}

fun Tag.pArticle(text: String) {
	val textList = text.split("\n")
	for (s in textList) {
		val t = addTag("p")
		t.style = "text-indent:2em"
		t.textEscaped(s).forView = true
	}
}

fun Tag.pArticle(block: () -> String) {
	pArticle(block())
}

fun Tag.targetBlank() {
	this.target = "_blank"
}

fun Tag.targetSelf() {
	this.target = "_self"
}

fun Tag.targetParent() {
	this.target = "_parent"
}

fun Tag.targetTop() {
	this.target = "_top"
}
