package dev.entao.speak

import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.*
import dev.entao.speak.model.Account
import dev.entao.speak.model.Speaker
import dev.entao.speak.model.UserDir

class AppGroup(context: HttpContext) : HttpGroup(context) {

	@NeedToken
	fun speakerListAction() {
		val w = (Speaker::flag EQ 0) AND (Speaker::userId EQ context.userId)
		val ls = Speaker.findAll(w) {
			asc(Speaker::id)
		}
		context.resultSender.arr(ls) {
			it.toJsonClient()
		}
	}

	fun loginAction(user: String, pwd: String) {
		val a = Account.findOne((Account::phone EQ user) AND (Account::pwd EQ pwd))
		if (a == null) {
			context.resultSender.failed("用户名或密码错误")
			return
		}
		val token = context.makeToken(a.id, a.phone, 0)
		context.resultSender.str(token)
	}

	@NeedToken
	fun mkdirAction(@NotEmpty dirName: String) {
		val tm = context.tokenModel ?: return
		val a = UserDir()
		a.userId = tm.userId
		a.dirName = dirName
		a.flag = 0
		a.insert()
		context.resultSender.ok()
	}

	@NeedToken
	fun rmdirAction(@NotEmpty dirId: Long) {
		val tm = context.tokenModel ?: return
		val dir = UserDir.findByKey(dirId)
		if (dir != null) {
			dir.updateByKey {
				dir.flag = 1
			}
		}
		context.resultSender.ok()
	}
}