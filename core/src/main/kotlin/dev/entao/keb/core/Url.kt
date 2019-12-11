@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.keb.core

import dev.entao.kava.base.urlDecoded
import dev.entao.kava.base.urlEncoded

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