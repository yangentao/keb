package dev.entao.keb.biz

import dev.entao.keb.biz.model.Account
import dev.entao.keb.biz.model.ResAccess
import dev.entao.keb.biz.model.TokenTable
import dev.entao.kava.base.base64Encoded
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.ParamConst
import dev.entao.keb.core.Router
import dev.entao.keb.core.Url

class DefaultPermAcceptor : dev.entao.keb.core.PermAcceptor {
	private val accessMap = HashMap<String, Int>()

	override fun prepare(context: dev.entao.keb.core.HttpContext) {
		val aid = context.accountId
		if(aid < 0) {
			return
		}
		val ac = Account.findByKey(aid )
		if (ac != null) {
			ResAccess.findAll(ResAccess::objId to ac.deptId, ResAccess::objType to ResAccess.TDept).forEach {
				accessMap[it.uri] = it.judge
			}
			ResAccess.findAll(ResAccess::objId to ac.id, ResAccess::objType to ResAccess.TAccount).forEach {
				if (it.judge == ResAccess.Allow) {
					accessMap[it.uri] = it.judge
				}
			}

		}
	}

	override fun accept(context: dev.entao.keb.core.HttpContext, uri: String): Boolean {
		if (!context.filter.webConfig.allowResAccess) {
			return true
		}
		val uu = uri.substringBefore('?')
		if (context.accountId == 0) {
			return true
		}
		val n = accessMap[uu] ?: return true
		return n == ResAccess.Allow

	}

}


object AuthAppAcceptor : dev.entao.keb.core.Acceptor {
	override fun accept(context: dev.entao.keb.core.HttpContext, router: Router): Boolean {
		context.os = context.httpParams.str("os") ?: ""
		context.token = context.httpParams.str(dev.entao.keb.core.HttpContext.TOKEN) ?: ""
		if (context.token.isNotEmpty()) {
			val t = TokenTable.findOne((TokenTable::token EQ context.token) AND (TokenTable::type EQ context.os))
			if (t != null && !t.isExpired) {
				context.userId = t.userId.toInt()
			}
		}
		if (router.needLoginApp && context.userId == 0) {
			context.resultSender.failed("未登录")
			return false
		}
		return true
	}

}

object AuthWebAcceptor : dev.entao.keb.core.Acceptor {
	override fun accept(context: dev.entao.keb.core.HttpContext, router: Router): Boolean {
		context.accountId = context.getSession(dev.entao.keb.core.HttpContext.ACCOUNT_ID)?.toIntOrNull() ?: 0

		if (router.needLoginWeb && context.accountId == 0) {
			val url = Url(context.filter.webConfig.loginUri)
			val q = context.request.queryString
			val burl = if (q != null && q.isNotEmpty()) {
				context.request.requestURL.toString() + "?" + q
			} else {
				context.request.requestURL.toString()
			}
			url.replace(ParamConst.BACK_URL, burl.base64Encoded)
			context.redirect(url.build())
			return false
		}

		return true
	}
}