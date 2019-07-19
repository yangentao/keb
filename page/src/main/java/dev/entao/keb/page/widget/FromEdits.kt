package dev.entao.keb.page.widget

import dev.entao.keb.page.*
import dev.entao.kava.base.*
import dev.entao.keb.core.MaxValue
import dev.entao.keb.core.MinValue
import dev.entao.keb.core.ParamConst
import dev.entao.keb.core.ValueRange
import dev.entao.keb.page.B
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.html.div
import dev.entao.keb.page.html.label
import kotlin.reflect.KProperty0
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2018/7/18.
 */

fun Tag.labelDateRow(labelText: String, nameValue: String, editBlock: Tag.() -> Unit = {}) {
	formGroupRow {
		val lb = this.label { +labelText }
		this.div {
			val ed = this.edit {
				this.name = nameValue
				this.pattern = "\\d{8}"
				this.maxlength = "8"
				this.placeholder = "8位日期数字, 如:20181201"
				value = httpContext.httpParams.str(nameValue) ?: ""
			}
			lb.forId = ed.needId()
			ed.editBlock()
		}
	}
}

fun Tag.labelEditRow(p: Prop, editBlock: Tag.() -> Unit = {}) {
	formGroupRow {
		val lb = this.label { +p.userLabel }
		this.div {
			val ed = this.edit { }
			editConfig(ed, p)
			ed.editBlock()
			labelConfig(lb, ed)
		}
	}
}

fun Tag.labelEditRow(labelText: String, editName: String, editBlock: Tag.() -> Unit = {}) {
	formGroupRow {
		val lb = this.label { +labelText }
		this.div {
			val ed = this.edit {
				name = editName
			}
			editConfig(ed, editName)
			ed.editBlock()
			labelConfig(lb, ed)
		}
	}
}

fun Tag.labelEditGroup(labelText: String, editName: String, editBlock: Tag.() -> Unit = {}) {
	formGroup {
		val lb = this.label { +labelText }
		val ed = this.edit { }
		editConfig(ed, editName)
		ed.editBlock()
		labelConfig(lb, ed)
	}
}

fun Tag.labelEditGroup(p: Prop, editBlock: Tag.() -> Unit = {}) {
	formGroup {
		labelEdit(p, editBlock)
	}
}

fun Tag.labelEdit(p: Prop, editBlock: Tag.() -> Unit = {}) {
	val lb = this.label { +p.userLabel }
	val ed = this.edit { }
	editConfig(ed, p)
	ed.editBlock()
	labelConfig(lb, ed)
}

fun Tag.labelTextAreaRow(p: Prop, editBlock: Tag.() -> Unit = {}) {
	formGroupRow {
		val pname = p.userName
		val er = httpContext.httpParams.str(ParamConst.err(pname)) ?: ""
		val lb = this.label { +p.userLabel }
		this.div {
			val ed = this.textarea {
				if (er.isNotEmpty()) {
					addClass(dev.entao.keb.page.B.isInValid)
				}
				name = pname
				rows = (p.findAnnotation<EditRows>()?.value ?: 3).toString()
				val v = if (p is KProperty0) {
					p.getValue()?.toString() ?: ""
				} else {
					httpContext.httpParams.str(pname) ?: ""
				}
				this.textEscaped(v)
				this.configEditOfProp(p)
			}
			if (er.isNotEmpty()) {
				this.invalidFeedback(er)
			}
			val hb = p.findAnnotation<FormHelpBlock>()?.value
			if (hb != null && hb.isNotEmpty()) {
				formTextMuted(hb)
			}
			ed.editBlock()
			labelConfig(lb, ed)
		}
	}
}




fun labelConfig(lb: Tag, ed: Tag) {
	lb.forId = ed.needId()
	if (ed.required) {
		var t = lb.parentTag
		while (t != null) {
			if (t.id == P.QUERY_FORM) {
				break
			}
			t = t.parentTag
		}
		if (t == null) {
			lb.textEscaped("*")
		} else {
			ed.required = false
		}
	}

}

fun editConfig(editTag: Tag, p: Prop) {
	val pname = p.userName
	val er = editTag.httpContext.httpParams.str(ParamConst.err(pname)) ?: ""
	editTag.apply {
		if (er.isNotEmpty()) {
			addClass(B.isInValid)
		}
		name = pname
		val v = if (p is KProperty0) {
			val vv = p.getValue()
			if (vv is Double) {
				val kd = p.findAnnotation<KeepDot>()
				if (kd != null) {
					vv.keepDot(kd.value)
				} else {
					vv.toString()
				}
			} else {
				vv?.toString() ?: ""
			}
		} else {
			httpContext.httpParams.str(pname) ?: ""
		}
		value = v
		this.configEditOfProp(p)
	}
	editTag.parentTag?.apply {
		if (er.isNotEmpty()) {
			this.invalidFeedback(er)
		}
		val hb = p.findAnnotation<FormHelpBlock>()?.value
		if (hb != null && hb.isNotEmpty()) {
			formTextMuted(hb)
		}
	}
}

fun Tag.configEditOfProp(p: Prop) {
	if (p.isTypeInt || p.isTypeLong || p.isTypeFloat || p.isTypeDouble) {
		typeNumber()
		val rr = p.findAnnotation<ValueRange>()
		if (rr != null) {
			min = rr.minVal
			max = rr.maxVal
		} else {
			val mr = p.findAnnotation<MinValue>()
			if (mr != null) {
				min = mr.value
			}
			val mr2 = p.findAnnotation<MaxValue>()
			if (mr2 != null) {
				max = mr2.value
			}
		}
		val stepAn = p.findAnnotation<StepValue>()
		if (stepAn != null) {
			this.step = stepAn.value
		} else {
			if (p.isTypeFloat || p.isTypeDouble) {
				val keepDot = p.findAnnotation<KeepDot>()
				if (keepDot != null) {
					val n = keepDot.value
					val f = Math.pow(0.1, n.toDouble())
					step = f.keepDot(n)
				} else {
					step = "0.000000001"
				}
			}
		}

	} else if (p.isTypeClass(java.sql.Date::class)) {
		typeDate()
	}
	val pat = p.findAnnotation<FormPattern>()
	if (pat != null) {
		this.pattern = pat.value
	}

	val ht = p.findAnnotation<FormHint>()
	if (ht != null) {
		this.placeholder = ht.value
	}

	val maxLen = p.findAnnotation<dev.entao.kava.sql.Length>()?.value ?: 0
	if (maxLen > 0) {
		maxlength = maxLen.toString()
	} else if (p.isTypeString) {
		maxlength = "256"
	}
	if (null != p.findAnnotation<FormRequired>()) {
		this.required = true
	}
	val fd = p.findAnnotation<FormDate>()
	if (fd != null) {
		this.typeDate()
	}
}

fun editConfig(editTag: Tag, editName: String) {
	val er = editTag.httpContext.httpParams.str(ParamConst.err(editName)) ?: ""
	editTag.apply {
		if (er.isNotEmpty()) {
			addClass(B.isInValid)
		}
		name = editName
		value = httpContext.httpParams.str(editName)  ?: ""
	}
	editTag.parentTag?.apply {
		if (er.isNotEmpty()) {
			this.invalidFeedback(er)
		}
	}
}

