package dev.entao.keb.core.account

import dev.entao.json.YsonObject
import dev.entao.keb.core.*
import dev.entao.keb.core.util.JWT


val HttpContext.userID: Long?
	get() {
		val userId = this.tokenUserId ?: return null
		if (userId == 0L) {
			return null
		}
		return userId
	}

var HttpContext.tokenUserId: Long?
	get() {
		return this.propMap["tokenUserId"] as? Long
	}
	set(value) {
		if (value == null) {
			this.propMap.remove("tokenUserId")
		} else {
			this.propMap["tokenUserId"] = value
		}
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
	var platform: String by yo

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
fun HttpContext.makeToken(userId: Long, userName: String, platform: String, expireTime: Long): String {
	val ts = this.filter.sliceList.find { it is TokenSlice } as? TokenSlice
			?: throw IllegalAccessError("没有找到TokenSlice")
	val m = TokenModel()
	m.userId = userId
	m.userName = userName
	m.expireTime = expireTime
	m.platform = platform
	return ts.makeToken(m.yo.toString())
}

val HttpContext.accessToken: String?
	get() {
		val a = this.request.header("Authorization")
		val b = a?.substringAfter("Bearer ", "")?.trim() ?: ""
		if (b.isNotEmpty()) {
			return b
		}
		val tk = this.request.param("access_token") ?: this.request.param("token") ?: return null
		return if (tk.isNotEmpty()) {
			tk
		} else {
			null
		}
	}

//override fun onInit() {
//	addSlice(TokenSlice("99665588"))
//}
class TokenSlice(val pwd: String) : HttpSlice {

	override fun beforeRequest(context: HttpContext) {
		val token = context.accessToken ?: return
		val j = JWT(pwd, token)
		context.jwtValue = j
		if (j.OK) {
			val m = TokenModel(YsonObject(j.body))
			context.tokenModel = m
			if (!m.expired) {
				context.tokenUserId = m.userId
			}
		}
	}

	override fun acceptRouter(context: HttpContext, router: Router): Boolean {
		if (router.needToken) {
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
