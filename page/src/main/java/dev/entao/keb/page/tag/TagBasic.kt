@file:Suppress("unused")

package dev.entao.keb.page.tag


fun Tag.a(vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("a", *kv, block = block)
}

fun Tag.button(vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("button", type_ to "button", *kv, block = block)
}

fun Tag.submit(vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("button", type_ to "submit", *kv, block = block)
}

fun Tag.link(vararg kv: HKeyValue): Tag {
	return this.tag("link", *kv, block = {})
}

fun Tag.meta(vararg kv: HKeyValue): Tag {
	return this.tag("meta", *kv, block = {})
}

fun Tag.script(src: String): Tag {
	return this.tag("script", type_ to "text/javascript", src_ to src, block = {})
}

fun Tag.script(block: () -> String): Tag {
	return this.tag("script", type_ to "text/javascript") {
		textUnsafe(block)
	}
}

fun Tag.option(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("option", *kv, block = block)
}

fun Tag.div(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("div", *kv, block = block)
}

fun Tag.form(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("form", *kv, block = block)
}

fun Tag.label(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("label", *kv, block = block)
}

fun Tag.img(vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("img", *kv, block = block)
}

fun Tag.span(vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("span", *kv, block = block)
}

fun Tag.hr(vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("hr", *kv, block = block)
}

fun Tag.pre(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("pre", *kv, block = block)
}

fun Tag.code(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("code", *kv, block = block)
}

fun Tag.var_(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("var", *kv, block = block)
}

fun Tag.kbd(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("kbd", *kv, block = block)
}

fun Tag.ol(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("ol", *kv, block = block)
}

fun Tag.ul(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("ul", *kv, block = block)
}

fun Tag.li(vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("li", *kv, block = block)
}

fun Tag.h1(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("h1", *kv, block = block)
}

fun Tag.h2(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("h2", *kv, block = block)
}

fun Tag.h3(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("h3", *kv, block = block)
}

fun Tag.h4(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("h4", *kv, block = block)
}

fun Tag.h5(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("h5", *kv, block = block)
}

fun Tag.h6(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("h6", *kv, block = block)
}

fun Tag.p(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("p", *kv, block = block)
}

fun Tag.dl(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("dl", *kv, block = block)
}

fun Tag.dt(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("dt", *kv, block = block)
}

fun Tag.dd(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("dd", *kv, block = block)
}

fun Tag.table(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("table", *kv, block = block)
}

fun Tag.thead(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("thead", *kv, block = block)
}


fun Tag.tbody(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("tbody", *kv, block = block)
}


fun Tag.th(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("th", *kv, block = block)
}


fun Tag.tr(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("tr", *kv, block = block)
}


fun Tag.td(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("td", *kv, block = block)
}


fun Tag.col(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("col", *kv, block = block)
}


fun Tag.colgroup(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("colgroup", *kv, block = block)
}


fun Tag.well(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("well", *kv, block = block)
}


fun Tag.strong(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("strong", *kv, block = block)
}


fun Tag.small(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("small", *kv, block = block)
}


fun Tag.nav(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("nav", *kv, block = block)
}


fun Tag.font(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("font", *kv, block = block)
}


fun Tag.font(size: Int, color: String, block: TagCallback) {
	this.font(size_ to size.toString(), color_ to color, block = block)
}


fun Tag.textarea(vararg vs: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("textarea", "rows" to "3", *vs, block = block)
}


fun Tag.input(vararg vs: HKeyValue, block: TagCallback = {}): Tag {
	return this.tag("input", *vs, block = block)
}


fun Tag.edit(vararg vs: HKeyValue, block: TagCallback = {}): Tag {
	return input(type_ to "text", *vs, block = block)
}


fun Tag.date(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "date", *vs, block = block)
}

fun Tag.time(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "time", *vs, block = block)
}

fun Tag.datetime(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "datetime", *vs, block = block)
}

fun Tag.file(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "file", *vs, block = block)
}

fun Tag.password(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "password", *vs, block = block)
}

fun Tag.email(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "email", *vs, block = block)
}

fun Tag.hidden(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "hidden", *vs, block = block)
}

fun Tag.radio(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "radio", *vs, block = block)
}

fun Tag.checkbox(vararg vs: HKeyValue, block: TagCallback): Tag {
	return input(type_ to "checkbox", *vs, block = block)
}

fun Tag.footer(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("footer", *kv, block = block)
}


fun Tag.article(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("article", *kv, block = block)
}

fun Tag.stylesheet(url: String) {
	link(rel_ to "stylesheet", href_ to url)
}