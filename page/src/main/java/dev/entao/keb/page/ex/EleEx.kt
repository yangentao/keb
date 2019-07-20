package dev.entao.keb.page.ex

import dev.entao.keb.core.ParamConst
import dev.entao.keb.page.alertError
import dev.entao.keb.page.alertSuccess
import dev.entao.keb.page.html.Tag

fun Tag.showMessagesIfPresent() {
	val s = this.httpContext.httpParams.str(ParamConst.ERROR) ?: ""
	if (s.isNotEmpty()) {
		this.alertError { +s }
	}
	val ss = this.httpContext.httpParams.str(ParamConst.SUCCESS) ?: ""
	if (ss.isNotEmpty()) {
		this.alertSuccess { +ss }
	}
}