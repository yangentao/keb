@file:Suppress("unused")

package dev.entao.keb.page.tag

import dev.entao.kava.base.*


fun Tag.a(text: String, href: String): Tag {
	return this.a(href_ to href) {
		+text
	}
}


fun Tag.br(): Tag {
	return tag("br")
}

fun Tag.hr(): Tag {
	return this.hr {}
}

fun Tag.label(text: String): Tag {
	return label {
		+text
	}
}


fun Tag.hidden(p: Prop0) {
	val v = p.getValue()?.toString() ?: ""
	this.hidden {
		name = p.userName
		this += value_ to v
	}
}

fun Tag.hidden(hiddenName: String, hiddenValue: Any?) {
	val v = hiddenValue?.toString() ?: ""
	this.hidden {
		name = hiddenName
		this += value_ to v
	}
}

fun Tag.hidden(p: Prop1, v: Any?) {
	val vv = v?.toString() ?: ""
	this.hidden {
		name = p.userName
		this += value_ to vv
	}
}

fun Tag.label(p: Prop): Tag {
	return this.label(for_ to p.userName) {
		+p.userLabel
	}
}

fun Tag.forField(p: Prop) {
	this += for_ to p.userName
}


fun Tag.mutedText(block: TagCallback): Tag {
	return this.small(class_ to _text_muted, block = block)
}

fun Tag.pArticle(text: String) {
	val textList = text.split("\n")
	for (s in textList) {
		this.p(style_ to "text-indent:2em") {
			textEscaped(s)?.forView = true
		}
	}
}

fun Tag.pArticle(block: () -> String) {
	pArticle(block())
}


