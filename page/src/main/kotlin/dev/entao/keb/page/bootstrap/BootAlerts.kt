package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*


fun Tag.alert(vararg kv: HKeyValue, block: TagCallback) {
	this.div(class_ to _alert.._alert_dismissible, role_ to "alert", *kv) {
		button(class_ to _close, "data-dismiss" to "alert", aria_label_ to "Close") {
			span(aria_hidden_ to "true") { !"&times;" }
		}
		this.block()
	}
}


fun Tag.alert(cls: HClass, text: String) {
	this.alert(class_ to cls) {
		+text
	}
}

fun Tag.alertSuccess(text: String) {
	this.alert(_alert_success, text)
}

fun Tag.alertError(text: String) {
	this.alert(_alert_danger, text)
}

fun Tag.alertWanning(text: String) {
	this.alert(_alert_warning, text)
}

fun Tag.alertInfo(text: String) {
	this.alert(_alert_info, text)
}