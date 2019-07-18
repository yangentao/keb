package dev.entao.keb.page.widget

import dev.entao.kava.base.*
import dev.entao.keb.core.html.FormHelpBlock
import dev.entao.keb.page.*
import kotlin.reflect.KProperty0
import kotlin.reflect.full.findAnnotation

private val InputTags = setOf("input", "select", "textarea")

fun Tag.formGroup(block: TagCallback): Tag {
	return div {
		clazz = "form-group"
		this.block()
		processStyleOfInput(this)
	}
}

private fun processStyleOfInput(divTag: Tag) {
	divTag.apply {
		var t = children.find { it.tagName in InputTags }
		if (t == null) {
			val dv = children.firstOrNull { it.tagName == "div" } ?: return
			t = dv.children.find { it.tagName in InputTags }
		}
		if (t == null) {
			return
		}
		findChild { it.tagName == "label" }?.needFor(t)
		if (t.tagName == "input") {
			if (t.type == "file") {
				t.addClassFirst("form-control-file")
				return
			}
			if (t.readonly) {
				t.addClassFirst("form-control-plaintext")
				return
			}
		}
		t.addClassFirst("form-control")
	}
}

fun Tag.formGroupRow(block: TagCallback): Tag {
	return flexRow {
		//		class_ = "form-group row"
		addClass("p-1")
		this.block()
		val lb = findChild { it.tagName == "label" }
		val dv = children.find { it.tagName == "div" } ?: return@flexRow

		if (lb == null) {
			dv.style = "margin-left:7em"
		} else {
			dv.style = "margin-left:0.5em"
		}
		dv.addClass("w-100")

		lb?.addClassFirst("col-form-label")
		lb?.style = "width:7em"

		processStyleOfInput(this)
	}
}

fun Tag.labelTextRow(p: Prop) {
	val pname = p.userName
	var v: Any? = null
	if (p is KProperty0) {
		v = p.getValue()
	} else {
		val s = httpContext.httpParams.str(pname)
		if (s != null) {
			v = strToV(s, p)
		}
	}
	val textVal = displayOf(p, v)
	this.labelTextRow(p.userLabel + ":", textVal)
}

fun Tag.labelTextRow(labelValue: String, textValue: String) {
	formGroupRow {
		this.label { +labelValue }
		this.div {
			this.span {
				classList += "form-control-plaintext"
				+textValue
			}
		}
	}
}

fun Tag.labelLinkRow(labelValue: String, linkText: String, linkHref: String) {
	formGroupRow {
		this.label { +labelValue }
		this.div {
			this.a(linkText, linkHref)
		}
	}
}

fun Tag.labelFileRow(label: String, name: String, value: String, helpText: String = "") {
	formGroupRow {
		label(label)
		this.div {
			uploadDiv(name, value)
			if (helpText.isNotEmpty()) {
				formTextMuted(helpText)
			}
		}
	}
}

fun Tag.labelFileRow(p: Prop) {
	formGroupRow {
		label(p.userLabel)
		this.div {
			uploadDiv(p)
			val hb = p.findAnnotation<FormHelpBlock>()?.value
			if (hb != null && hb.isNotEmpty()) {
				formTextMuted(hb)
			}
		}
	}
}
