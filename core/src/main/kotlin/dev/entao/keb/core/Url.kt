@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.keb.core

import dev.entao.kava.base.firstParamName
import dev.entao.kava.base.urlDecoded
import dev.entao.kava.base.urlEncoded
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.KFunction

/**
 * Created by entaoyang@163.com on 2018/4/5.
 */

class UriMake(val context: HttpContext, val action: KFunction<*>) {

	val map: LinkedHashMap<String, String> = LinkedHashMap<String, String>()

	fun param(value: Any?): UriMake {
		if (value != null) {
			val k = action.firstParamName
			if (k != null) {
				map[k] = value.toString()
			}
		}
		return this
	}

	fun arg(key: String, value: Any?): UriMake {
		if (value != null) {
			map[key] = value.toString()
		}
		return this
	}

	val fullUrl: String
		get() {
			return context.fullUrlOf(uri)
		}
	val uri: String
		get() {
			val a = context.actionUri(action)
			return if (map.isEmpty()) {
				a
			} else {
				"$a?" + map.map {
					it.key + "=" + URLEncoder.encode(it.value, Charsets.UTF_8.name())
				}.joinToString("&")
			}
		}
}

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
//数组,  k[]=1&k[]=2
open class Url(s: String) {

	//http://localhost/a/b
	private val rawUrl: String
	//[name to yang, age to 22, add to ""]
	val params = ArrayList<Pair<String, String>>()

	init {
		val ls = s.split('?', limit = 2)
		if (ls.size <= 1) {
			rawUrl = s
		} else {
			rawUrl = ls[0]
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
			return rawUrl
		}
		val q = params.map { it.first + "=" + it.second.urlEncoded }
		return rawUrl + "?" + q.joinToString("&")
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


	init {
		withParams()
	}

	private fun withParams(): ReferUrl {
		val map = request.paramMap
		map.forEach {
			val k = it.key
			val ar = it.value
			this.remove(k)
			if (ar.size == 1) {
				this.append(k, ar[0])
			} else if (ar.size > 1) {
				for (v in ar) {
					this.append("$k[]", v)
				}
			}
		}
		return this
	}

	fun exclude(ks: Set<String>): ReferUrl {
		this.params.removeAll { it.first in ks }
		return this
	}

	fun keep(ks: Set<String>): ReferUrl {
		this.params.removeAll { it.first !in ks }
		return this
	}

	fun clearParams(): ReferUrl {
		this.params.clear()
		return this
	}

	fun arg(key: String, value: String): ReferUrl {
		this.replace(key, value)
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