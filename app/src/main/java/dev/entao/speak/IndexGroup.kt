package dev.entao.speak

import dev.entao.kava.base.Label
import dev.entao.kava.qr.QRImage
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.Keb
import dev.entao.keb.core.LoginAction
import dev.entao.keb.page.tag.TagCallback
import dev.entao.speak.model.Account

@Label("员工")
class IndexGroup(context: HttpContext) : HttpGroup(context) {




	@LoginAction
	fun loginAction() {
		resultSender.ok()
	}



}