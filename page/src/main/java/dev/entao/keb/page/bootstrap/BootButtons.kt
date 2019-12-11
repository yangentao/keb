@file:Suppress("unused")

package dev.entao.keb.page.bootstrap

import dev.entao.kava.base.firstParamName
import dev.entao.kava.base.userLabel
import dev.entao.keb.core.ActionURL
import dev.entao.keb.core.HttpAction
import dev.entao.keb.page.*
import dev.entao.keb.page.tag.*
import kotlin.reflect.full.findAnnotation


fun Tag.a(action: HttpAction): Tag {
	return this.a {
		this += action
	}
}

fun Tag.a(action: ActionURL): Tag {
	return this.a {
		this += action
	}
}

fun Tag.a(action: HttpAction, text: String): Tag {
	return this.a {
		+text
		this += action
	}
}

fun Tag.a(action: ActionURL, text: String): Tag {
	return this.a {
		+text
		this += action
	}
}


fun Tag.linkButton(vararg kv: HKeyValue, block: TagCallback): Tag {
	return a(role_ to "button", class_ to _btn, *kv, block = block)
}


fun Tag.submit(text: String = "提交"): Tag {
	return this.submit(class_ to _btn) { +text }
}

fun Tag.submitPrimary(text: String = "提交"): Tag {
	return this.submit(class_ to _btn.._btn_primary) { +text }
}


fun Tag.buttonB(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.button(class_ to _btn, *kv, block = block)
}

fun Tag.buttonTheme(theme: HClass, vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.button(class_ to _btn..theme, *kv, block = block)
}

fun Tag.buttonPrimary(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.buttonTheme(_btn_primary, *kv, block = block)
}


fun Tag.buttonApplyTheme(action: HttpAction, defaultTheme: String): Tag {
	val m = action.findAnnotation<ActionTheme>()
	if (m != null) {
		if (m.value.isNotEmpty()) {
			this += m.value
		}
	} else {
		if (action.findAnnotation<ActionDanger>() != null) {
			this += _btn_danger
		} else if (action.findAnnotation<ActionSafe>() != null) {
			this += _btn_success
		} else if (action.findAnnotation<ActionPrimary>() != null) {
			this += _btn_primary
		} else {
			if (defaultTheme.isNotEmpty()) {
				this += defaultTheme
			}
		}
	}
	return this
}

fun Tag.buttonX(action: HttpAction, vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	val t = button(class_ to _btn, *kv) {
		+action.userLabel
		this += action
		needId()
		this[data_param_name_] = action.firstParamName ?: "id"
		this[data_confirm_] = action.findAnnotation<FormConfirm>()?.value ?: ""
	}
	t.block()
	return t
}

fun Tag.buttonX(actionURL: ActionURL, vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	val action = actionURL.action
	val t = button(class_ to _btn, *kv) {
		+action.userLabel
		this += actionURL
		needId()
		this[data_param_name_] = action.firstParamName ?: "id"
		this[data_confirm_] = action.findAnnotation<FormConfirm>()?.value ?: ""
	}
	t.block()
	return t
}


fun Tag.linkButtonX(action: HttpAction, vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	val t = linkButton(*kv) {
		this += action
	}
	t.block()
	return t
}

fun Tag.linkButtonX(actionURL: ActionURL, vararg kv: HKeyValue, block: TagCallback = {}): Tag {
	val t = linkButton(*kv) {
		this += actionURL
	}
	t.block()
	return t
}


fun Tag.submitAsync(respJS: String = ""): Tag {
	val formTag = this.parent { it.tagName == "form" } ?: return this
	val formId = formTag.needId()
	script {
		"""
			$('#$formId').submit(function(e){
				e.preventDefault();
				var fm = $('#$formId');
				var data = fm.serialize();
				$.post(fm.attr('action'), data, function(resp){
						$respJS;
				});
				return false ;
			})
			"""
	}
	return this
}


fun Tag.confirm(text: String): Tag {
	this[data_confirm_] = text
	if (this.tagName == "a") {
		this[onclick_] = "var s = $(this).attr('data-confirm');return !s || confirm(s);"
	}
	return this
}

fun Tag.confirm(action: HttpAction): Tag {
	val fcValue = action.findAnnotation<FormConfirm>()?.value ?: return this
	this[data_confirm_] = fcValue
	if (this.tagName == "a") {
		this[onclick_] = "var s = $(this).attr('data-confirm');return !s || confirm(s);"
	}
	return this
}


fun Tag.onClickReload() {
	this[onclick_] = "reloadLink(this); return false ;"
}

fun Tag.onClickOpenDialog() {
	this[onclick_] = "openDialogPanel(this); return false;"
}
