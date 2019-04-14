package dev.entao.ken.ex

import dev.entao.kage.P
import dev.entao.sql.AND
import dev.entao.sql.EQ
import dev.entao.kbase.base64Encoded
import dev.entao.ken.HttpContext
import yet.servlet.Ip
import yet.servlet.Router
import yet.servlet.TokenTable
import yet.servlet.Url
import yet.web.model.Account
import yet.web.model.User

interface Acceptor {
	fun accept(context: HttpContext, router: Router): Boolean
}

object MethodAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		if (router.methods.isNotEmpty()) {
			if (context.request.method.toUpperCase() !in router.methods) {
				context.abort(400, "Method Error")
				return false
			}
		}
		return true
	}

}

object AuthAppAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		context.os = context.httpParams.str("os") ?: ""
		context.token = context.httpParams.str(HttpContext.TOKEN) ?: ""
		if (context.token.isNotEmpty()) {
			val t = TokenTable.findOne((TokenTable::token EQ context.token) AND (TokenTable::type EQ context.os))
			if (t != null && !t.isExpired) {
				context.userId = t.userId.toInt()
			}
		}
		if (context.userId > 0) {
			val u = User.findByKey(context.userId)
			if (u != null) {
				if (u.status == 0) {
					context.user = u
				} else {
					context.user = null
					context.userId = 0
				}
			}
		}
		if (router.needLoginApp && context.userId == 0) {
			context.resultSender.failed("未登录")
			return false
		}
		return true
	}

}

object AuthWebAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		context.accountId = context.getSession(HttpContext.ACCOUNT_ID)?.toIntOrNull() ?: 0

		if (context.accountId > 0) {
			val a = Account.findByKey(context.accountId)
			if (a != null) {
				if (a.status == 0) {
					context.account = a
				} else {
					context.account = null
					context.accountId = 0
				}
			}
		}
		if (router.needLoginWeb && context.accountId == 0) {
			val url = Url(context.filter.webConfig.loginUri)
			val q = context.request.queryString
			val burl = if (q != null && q.isNotEmpty()) {
				context.request.requestURL.toString() + "?" + q
			} else {
				context.request.requestURL.toString()
			}
			url.replace(P.BACK_URL, burl.base64Encoded)
			context.redirect(url.build())
			return false
		}

		return true
	}
}

object IpAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		Ip.save(context)
		return true
	}
}

object ResAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		context.prepareAccess()
		val b = context.allow(router.uri)
		if (!b) {
			if (context.acceptJson) {
				context.resultSender.failed("未授权")
			} else {
				context.htmlSender.print("未授权")
			}
		}
		return b
	}

}