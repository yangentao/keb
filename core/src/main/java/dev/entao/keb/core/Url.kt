@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.keb.core

import dev.entao.kava.base.urlDecoded
import dev.entao.kava.base.urlEncoded
import javax.servlet.http.HttpServletRequest

/**
 * Created by entaoyang@163.com on 2018/4/5.
 */

//fun main(args: Array<String>) {
//	paramsOfUrl("http://localhost/a/b?name=yang&age=22&add=")
//}
//
//fun paramsOfUrl(s: String) {
//	val u = Url(s)
//	logd(u.url)
//	for (p in u.params) {
//		logd(p.first, "=>", p.second)
//	}
//}

//http://localhost/a/b?name=yang&age=22&add=
open class Url(s: String) {

	//http://localhost/a/b
	val url: String
	//[name to yang, age to 22, add to ""]
	val params = ArrayList<Pair<String, String>>()

	init {
		val ls = s.split('?', limit = 2)
		if (ls.size <= 1) {
			url = s
		} else {
			url = ls[0]
			val qls = ls[1].split('&')

			qls.forEach {
				val ar = it.split('=', limit = 2)
				val p = if (ar.size == 1) {
					Pair(ar[0], "")
				} else {
					Pair(ar[0], ar[1].urlDecoded)
				}
				params.add(p)
			}

		}
	}

	fun build(): String {
		if (params.isEmpty()) {
			return url
		}
		val q = params.map { it.first + "=" + it.second.urlEncoded }
		return url + "?" + q.joinToString("&")
	}

	fun append(key: String, value: String): Url {
		params.add(Pair(key, value))
		return this
	}

	fun replace(key: String, value: String) {
		params.removeAll { it.first == key }
		params.add(Pair(key, value))
	}

	fun remove(key: String) {
		params.removeAll { it.first == key }
	}
}

class ReferUrl(val request: HttpServletRequest) : Url(request.headerReferer!!) {
	fun withReqParam(): ReferUrl {
		val map = request.paramMap
		map.forEach {
			val k = it.key
			val ar = it.value
			this.remove(k)
			if (!ParamConst.isErr(k)) {
				if (ar.size == 1) {
					this.append(k, ar[0])
				} else if (ar.size > 1) {
					for (v in ar) {
						this.append("$k[]", v)
					}
				}
			}
		}
		return this
	}

	fun arg(key: String, value: String): ReferUrl {
		this.replace(key, value)
		return this
	}

	fun ok(msg: String): ReferUrl {
		arg(ParamConst.SUCCESS, msg)
		return this
	}

	//上面显示的一个错误信息条
	fun err(msg: String): ReferUrl {
		arg(ParamConst.ERROR, msg)
		return this
	}

	//具体某个字段的错误信息
	fun errOf(name: String, msg: String): ReferUrl {
		arg(ParamConst.err(name), msg)
		return this
	}

	infix fun String.to(value: String) {
		this@ReferUrl.arg(this, value)
	}

	infix fun String.to(value: Int) {
		this@ReferUrl.arg(this, value.toString())
	}

	infix fun String.to(value: Long) {
		this@ReferUrl.arg(this, value.toString())
	}
}