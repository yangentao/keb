package dev.entao.biz

import dev.entao.ken.HttpContext
import dev.entao.biz.model.User
import dev.entao.biz.model.TokenTable
import dev.entao.sql.AND
import dev.entao.sql.EQ

fun HttpContext.loginApp(phone: String, pwd: String): User? {
	val u = User.findOne(User::phone EQ phone AND (User::pwd EQ pwd))
	if (u == null) {
		resultSender.failed("用户名或密码错误")
		return null
	}
	if (u.status != 0) {
		resultSender.failed("帐号已禁用")
		return null
	}
	return u
}

fun HttpContext.saveToken(appUserId: Int, os: String): String {
	return TokenTable.refresh(appUserId.toString(), os, 0L)
}

fun HttpContext.logoutApp() {
	if (userId > 0) {
		TokenTable.remove(userId.toString(), os)
	}
	userId = 0
}