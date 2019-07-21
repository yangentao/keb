package dev.entao.keb.page.tag

val Tag.rootTag: Tag
	get() {
		var p = this
		while (p.parentTag != null) {
			p = p.parentTag!!
		}
		return p
	}

val Tag.htmlTag: Tag?
	get() {
		val r = rootTag
		if (r.tagName == "html") {
			return r
		}
		return r.findChild { it.tagName == "html" }
	}
val Tag.headTag: Tag?
	get() {
		return this.rootTag.findChildDeep { it.tagName == "head" }
	}

val Tag.bodyTag: Tag?
	get() {
		return this.rootTag.findChildDeep { it.tagName == "body" }
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
