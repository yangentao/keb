package dev.entao.keb.page.bootstrap

import dev.entao.kava.base.*
import dev.entao.keb.core.*
import dev.entao.keb.page.*
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.uploadDiv
import kotlin.reflect.full.findAnnotation

private val InputTags = setOf("input", "select", "textarea")


fun Tag.formGroupText(labelValue: String, textValue: String) {
	formGroup {
		this.label { +labelValue }
		this.div {
			this.span(class_ to _form_control_plaintext) {
				+textValue
			}
		}
	}
}

fun Tag.formGroupText(textValue: String) {
	formGroup {
		this.div {
			this.span(class_ to _form_control_plaintext) {
				+textValue
			}
		}
	}
}

fun Tag.formGroupUpload(p: Prop): Tag {
	return formGroup {
		label(p.userLabel)
		this.div {
			uploadDiv(p)
			this.processHelpText(p)
		}
	}
}


fun Tag.formGroupEdit(labelText: String, editName: String, editBlock: Tag.() -> Unit = {}) {
	formGroup {
		val lb = this.label { +labelText }
		val ed = this.edit {
			this[name_] = editName
		}
		this.processGroupEditError(ed)
		ed.editBlock()
		processLabelRequire(lb, ed)
	}
}

fun Tag.formGroupEdit(p: Prop, block: TagCallback = {}) {
	formGroup {
		val lb = label(p)
		val ed = edit(p)
		this.processGroupEditError(ed)
		this.processHelpText(p)
		processLabelRequire(lb, ed)
		ed.block()
	}
}

fun Tag.formGroupTextArea(p: Prop, block: TagCallback = {}) {
	formGroup {
		val lb = label(p)
		val ed = textarea(p)
		this.processGroupEditError(ed)
		this.processHelpText(p)
		processLabelRequire(lb, ed)
		ed.block()
	}
}

fun Tag.formGroup(block: TagCallback): Tag {
	return div(class_ to _form_group) {
		this.block()
		processControlCSS()
	}
}

fun Tag.edit(p: Prop): Tag {
	val v = valueByProp(p)
	val pName = p.userName
	val ed = edit(name_ to pName, id_ to pName, value_ to v)
	ed.processPropertiesOfEdit(p)
	return ed
}

fun Tag.textarea(p: Prop): Tag {
	val v = valueByProp(p)
	val pName = p.userName
	return textarea(name_ to pName, id_ to pName) {
		+v
		this += rows_ to (p.findAnnotation<EditRows>()?.value ?: 3).toString()
	}
}

private fun Tag.valueByProp(p: Prop): String {
	if (p !is Prop0) {
		return httpContext.httpParams.str(p.userName) ?: ""
	}

	val vv = p.getValue() ?: return httpContext.httpParams.str(p.userName) ?: ""

	if (vv is Double) {
		val kd = p.findAnnotation<KeepDot>()
		return if (kd != null) {
			vv.keepDot(kd.value)
		} else {
			vv.toString()
		}
	}
	return vv.toString()

}

fun Tag.processHelpText(p: Prop) {
	val hb = p.findAnnotation<FormHelpBlock>()?.value
	if (hb != null && hb.isNotEmpty()) {
		formTextMuted(hb)
	}
}

private fun Tag.processGroupEditError(ed: Tag) {
	val er = httpContext.httpParams.str(Keb.errField(ed[name_])) ?: ""
	if (er.isNotEmpty()) {
		ed += "is-invalid"
		feedbackInvalid(er)
	}
}

private fun processLabelRequire(lb: Tag, ed: Tag) {
	lb[for_] = ed.needId()
	if (ed[required_] == "true") {
		val queryForm = lb.parent { it.id == P.QUERY_FORM }
		if (queryForm == null) {
			lb.textEscaped("*")
		} else {
			ed += required_ to "false"
		}
	}

}


private fun Tag.processControlCSS() {
	val t = this.firstDeep { it.tagName in InputTags } ?: return
	if (t.tagName == "input") {
		if (t["type"] == "file") {
			if (!t.hasClass(_form_control_file)) {
				t[class_] = _form_control_file..t[class_]
			}
			return
		}
		if (t[readonly_] == "true") {
			if (!t.hasClass(_form_control_plaintext)) {
				t[class_] = _form_control_plaintext..t[class_]
			}
			return
		}
	}
	val tp = t[type_]
	if (tp != "radio" && tp != "checkbox") {
		if (!t.hasClass(_form_control)) {
			t[class_] = _form_control..t[class_]
		}
	}
}

private fun Tag.processPropertiesOfEdit(p: Prop) {
	if (p.isTypeInt || p.isTypeLong || p.isTypeFloat || p.isTypeDouble) {
		this += type_ to "number"
		val rr = p.findAnnotation<ValueRange>()
		if (rr != null) {
			this += min_ to rr.minVal
			this += max_ to rr.maxVal
		} else {
			val mr = p.findAnnotation<MinValue>()
			if (mr != null) {
				this += min_ to mr.value
			}
			val mr2 = p.findAnnotation<MaxValue>()
			if (mr2 != null) {
				this += max_ to mr2.value
			}
		}
		val stepAn = p.findAnnotation<StepValue>()
		if (stepAn != null) {
			this += step_ to stepAn.value
		} else {
			if (p.isTypeFloat || p.isTypeDouble) {
				val keepDot = p.findAnnotation<KeepDot>()
				if (keepDot != null) {
					val n = keepDot.value
					val f = Math.pow(0.1, n.toDouble())
					this += step_ to f.keepDot(n)
				} else {
					this += step_ to "0.000000001"
				}
			}
		}
	} else if (p.isTypeClass(java.sql.Date::class)) {
		this += type_ to V.date
	}

	val ht = p.findAnnotation<FormHint>()
	if (ht != null) {
		this += placeholder_ to ht.value
	}

	val lenRange = p.findAnnotation<LengthRange>()
	if (lenRange != null) {
		this += maxlength_ to lenRange.maxValue.toString()
		this += pattern_ to ".{${lenRange.minValue},}"
	} else {
		val maxLen = p.findAnnotation<Length>()?.value ?: 0
		if (maxLen > 0) {
			this += maxlength_ to maxLen.toString()
		} else if (p.isTypeString) {
			this += maxlength_ to "256"
		}

		val minLen = p.findAnnotation<MinLength>()?.value ?: 0
		if (minLen > 0) {
			this += pattern_ to ".{${minLen},}"
		}

	}
	if (null != p.findAnnotation<FormRequired>()) {
		this += required_ to "true"
	}
	val fd = p.findAnnotation<FormDate>()
	if (fd != null) {
		this += type_ to "date"
	}
	if (p.hasAnnotation<FormPassword>()) {
		this += type_ to "password"
	}
	if (p.hasAnnotation<FormEmail>()) {
		this += type_ to "email"
	}
	val pat = p.findAnnotation<FormPattern>()
	if (pat != null) {
		this += pattern_ to pat.value
	}

}


fun Tag.formCheck(vararg vs: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _form_check, *vs) {
		this.block()
		val lb = this.first { it.tagName == "label" }
		val cb = this.children.find { it.tagName == "input" && (it["type"] == "checkbox" || it["type"] == "radio") }
		if (cb != null) {
			if (!cb.hasClass(_form_check_input)) {
				cb[class_] = _form_check_input..cb[class_]
			}
		}
		if (lb != null) {
			if (!lb.hasClass(_form_check_label)) {
				lb[class_] = _form_check_label..lb[class_]
			}
		}
		lb?.needFor(cb)
	}
}

fun Tag.feedbackInvalid(text: String) {
	if (text.isNotEmpty()) {
		div(class_ to _invalid_feedback) {
			+text
		}
	}
}


fun Tag.formTextMuted(block: TagCallback): Tag {
	return tag("small", class_ to _form_text.._text_muted, block = block)
}


fun Tag.formTextMuted(text: String) {
	formTextMuted {
		+text
	}
}

