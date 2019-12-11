package dev.entao.keb.core.account

import dev.entao.kava.json.YsonObject
import dev.entao.keb.core.*
import dev.entao.keb.core.util.JWT

var HttpContext.userIdToken: Long
	get() {
		return (this.propMap["_userIdToken_"] as? Long) ?: 0L
	}
	set(value) {
		this.propMap["_userIdToken_"] = value
	}

var HttpContext.tokenModel: TokenModel?
	get() {
		return this.propMap["_tokenModel_"] as? TokenModel
	}
	set(value) {
		this.propMap["_tokenModel_"] = value
	}

//解析后的app传来的access_token


var HttpContext.jwtValue: JWT?
	get() {
		return this.propMap["_jwt_"] as? JWT
	}
	set(value) {
		this.propMap["_jwt_"] = value
	}


class TokenModel(val yo: YsonObject = YsonObject()) {
	var userId: Long by yo
	var userName: String by yo
	var expireTime: Long by yo

	val expired: Boolean
		get() {
			if (expireTime != 0L) {
				return System.currentTimeMillis() >= expireTime
			}
			return false
		}

	val valid: Boolean
		get() {
			return this.userName.isNotEmpty() && !this.expired
		}
}

//0 永不过期
fun HttpContext.makeToken(userId: Long, userName: String, expireTime: Long): String {
	val ts = this.filter.sliceList.find { it is TokenSlice } as? TokenSlice
			?: throw IllegalAccessError("没有找到TokenSlice")
	val m = TokenModel()
	m.userId = userId
	m.userName = userName
	m.expireTime = expireTime
	return ts.makeToken(m.yo.toString())
}

//override fun onInit() {
//	addSlice(TokenSlice("99665588"))
//}
class TokenSlice(val pwd: String) : HttpSlice {

	override fun beforeRequest(context: HttpContext) {
		val a = context.request.header("Authorization")
		val b = a?.substringAfter("Bearer ", "")?.trim() ?: ""
		val token = if (b.isEmpty()) {
			context.request.param("access_token") ?: context.request.param("token") ?: return
		} else {
			b
		}
		if (token.isEmpty()) {
			return
		}
		val j = JWT(pwd, token)
		context.jwtValue = j
		val m = TokenModel(YsonObject(j.body))
		context.tokenModel = m
		if (!m.expired) {
			context.userIdToken = m.userId
		}
	}

	override fun acceptRouter(context: HttpContext, router: Router): Boolean {
		if (router.function.isNeedToken) {
			val m = context.tokenModel
			if (m == null) {
				context.abort(401, "未登录")
				return false
			} else if (m.expired) {
				context.abort(401, "验证信息过期,请重新登录")
				return false
			}
		}
		return true
	}

	fun makeToken(body: String): String {
		return makeToken(body, """{"alg":"HS256","typ":"JWT"}""")
	}

	fun makeToken(body: String, header: String): String {
		return JWT.make(pwd, body, header)
	}
}
