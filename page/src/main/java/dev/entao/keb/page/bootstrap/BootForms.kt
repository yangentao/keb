package dev.entao.keb.page.bootstrap

import dev.entao.kava.base.*
import dev.entao.keb.core.*
import dev.entao.keb.page.*
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.uploadDiv
import java.text.DecimalFormat
import kotlin.math.pow
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
		val kd = p.findAnnotation<NumberFormat>()
		return if (kd != null) {
			val fmt = DecimalFormat(kd.pattern)
			val n = fmt.maximumFractionDigits
			vv.maxFraction(n)
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
	} else if (p.isTypeClass(java.sql.Date::class)) {
		this += type_ to V.date
	}
	val stepAn = p.findAnnotation<StepValue>()
	if (stepAn == null) {
		if (p.isTypeFloat || p.isTypeDouble) {
			val keepDot = p.findAnnotation<NumberFormat>()
			if (keepDot != null && keepDot.pattern.isNotEmpty()) {
				val fmt = DecimalFormat(keepDot.pattern)
				val n = fmt.maximumFractionDigits
				if (n > 0) {
					this += step_ to 10.toDouble().pow(-n).toString()
				} else {
					this += step_ to "1"
				}
			} else {
				this += step_ to "1"
			}
		}
	}
	val anList = p.annotations
	for (an in anList) {
		when (an) {
			is FormRequired -> this += required_ to "true"
			is FormHint -> this += placeholder_ to an.value
			is FormDate -> this += type_ to "date"
			is FormPassword -> this += type_ to "password"
			is FormEmail -> this += type_ to "email"
			is FormPattern -> this += pattern_ to an.value
			is FormPhone -> {
				this += type_ to "text"
				this += pattern_ to """^1[3,4,5,6,7,8,9]\d{9}$"""
			}
			is FormTel -> {
				this += type_ to "text"
				this += pattern_ to """^(\d{3,4}\-?)?\d{6,8}$"""
			}
			is LengthRange -> {
				this += maxlength_ to an.maxValue.toString()
				this += pattern_ to ".{${an.minValue},}"
			}
			is MinLength -> {
				if (an.value > 0) {
					this += pattern_ to ".{${an.value},}"
				}
			}
			is Length -> {
				val hasRange = null != anList.find { it is LengthRange }
				if (!hasRange) {
					if (an.value > 0) {
						this += maxlength_ to an.value.toString()
					} else if (p.isTypeString) {
						this += maxlength_ to "256"
					}
				}
			}
			is MinValue -> this += min_ to an.value
			is MaxValue -> this += max_ to an.value
			is ValueRange -> {
				this += min_ to an.minVal
				this += max_ to an.maxVal
			}
			is StepValue -> this += step_ to an.value
		}
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

