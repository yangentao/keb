package dev.entao.keb.page.bootstrap

import dev.entao.kava.base.*
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.formOptionsMap


fun Tag.propValue(p: Prop, defaultValue: String? = null): String? {
	return if (p is Prop0) {
		p.getValue()?.toString() ?: httpContext.httpParams.str(p.userName) ?: defaultValue
	} else {
		httpContext.httpParams.str(p.userName) ?: defaultValue
	}
}


fun Tag.formGroupRadioStatic(p: Prop, defaultValue: String? = null, inlines: Boolean = false): Tag {
	return formGroup {
		val pname = p.userName
		var selVal: String? = propValue(p, defaultValue)

		this.label { +p.userLabel }
		val ls = p.formOptionsMap
		ls.forEach { opt ->
			formCheck {
				if (inlines) {
					this += _form_check_inline
				}
				val r = radio(name_ to pname, value_ to opt.key) {
					if (selVal == null) { //选中第一个
						this += checked_ to "checked"
						selVal = this[value_]
					} else if (selVal == opt.key) {
						this += checked_ to "checked"
					}
				}
				label(opt.value)[for_] = r.needId()
			}
		}
		this.processHelpText(p)
	}
}

fun Tag.formGroupCheckStatic(p: Prop, defaultValue: String? = null, inlines: Boolean = false): Tag {
	return formGroup {
		val pname = p.userName
		var selVal: String? = propValue(p, defaultValue)

		this.label { +p.userLabel }
		val ls = p.formOptionsMap
		ls.forEach { opt ->
			formCheck {
				if (inlines) {
					this += _form_check_inline
				}
				val r = checkbox(name_ to pname, value_ to opt.key) {
					if (selVal == null) { //选中第一个
						this += checked_ to "checked"
						selVal = this[value_]
					} else if (selVal == opt.key) {
						this += checked_ to "checked"
					}
				}
				label(opt.value)[for_] = r.needId()
			}
		}
		this.processHelpText(p)
	}
}


