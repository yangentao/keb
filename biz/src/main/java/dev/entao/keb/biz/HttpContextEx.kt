package dev.entao.keb.biz

import dev.entao.keb.biz.model.TokenTable
import dev.entao.keb.biz.model.User
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.render.ResultRender

fun dev.entao.keb.core.HttpContext.loginApp(phone: String, pwd: String): User? {
	val u = User.findOne(User::phone EQ phone AND (User::pwd EQ pwd))
	if (u == null) {
		ResultRender(this).failed("用户名或密码错误")
		return null
	}
	if (u.status != 0) {
		ResultRender(this).failed("帐号已禁用")
		return null
	}
	return u
}

fun dev.entao.keb.core.HttpContext.saveToken(appUserId: Int, os: String): String {
	return TokenTable.refresh(appUserId.toString(), os, 0L)
}

fun dev.entao.keb.core.HttpContext.logoutApp() {
	if (userId > 0) {
		TokenTable.remove(userId.toString(), os)
	}
	userId = 0
}