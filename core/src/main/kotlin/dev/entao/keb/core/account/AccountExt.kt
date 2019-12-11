package dev.entao.keb.core.account

import dev.entao.kava.base.base64Decoded
import dev.entao.kava.base.base64Encoded
import dev.entao.kava.base.hasAnnotation
import dev.entao.keb.core.*

//web登录成功后, 设置account和accountName

var HttpContext.accountId: Long
	get() {
		return getSession(Keb.ACCOUNT_ID)?.toLongOrNull() ?: 0L
	}
	set(value) {
		if (value == 0L) {
			this.removeSession(Keb.ACCOUNT_ID)
		} else {
			putSession(Keb.ACCOUNT_ID, value.toString())
		}
	}
var HttpContext.account: String
	get() {
		return getSession(Keb.ACCOUNT) ?: ""
	}
	set(value) {
		if (value.isEmpty()) {
			this.removeSession(Keb.ACCOUNT)
		} else {
			putSession(Keb.ACCOUNT, value)
		}
	}

var HttpContext.accountName: String
	get() {
		val a = this.account
		if (a.isEmpty()) {
			return ""
		}
		return getSession(Keb.ACCOUNT_NAME) ?: a
	}
	set(value) {
		putSession(Keb.ACCOUNT_NAME, value)
	}

fun HttpContext.clearAccountInfo() {
	this.accountId = 0L
	this.account = ""
	this.accountName = ""
}

val HttpContext.isAccountLogined: Boolean
	get() {
		return account.isNotEmpty()
	}


object LoginCheckSlice : HttpSlice {

	override fun acceptRouter(context: HttpContext, router: Router): Boolean {
		if (router.function.hasAnnotation<NeedLogin>() || router.cls.hasAnnotation<NeedLogin>()) {
			if (!context.isAccountLogined) {
				if (context.filter.loginUri.isNotEmpty()) {
					if (context.acceptHtml) {
						var url = context.request.requestURI
						val qs = context.request.queryString ?: ""
						if (qs.isNotEmpty()) {
							url = "$url?$qs"
						}
						url = url.base64Encoded
						val u = Url(context.filter.loginUri)
						u.replace(Keb.BACK_URL, url)
						context.redirect(u.build())
						return false
					}
				}
				context.abort(401)
				return false
			}
		}
		return true
	}
}

val HttpContext.backURL: String?
	get() {
		return this.httpParams.str(Keb.BACK_URL)?.base64Decoded
	}