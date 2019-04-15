@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.keb.core

import dev.entao.kava.base.Prop1
import dev.entao.kava.base.getValue
import dev.entao.kava.base.ownerClass
import dev.entao.kava.base.userName
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import kotlin.math.min
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty0
import kotlin.reflect.full.valueParameters

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

class WebPath(val filter: HttpFilter) {

	private val list = ArrayList<String>()
	private var action: KFunction<*>? = null
	private val params: HashMap<String, String?> = HashMap()

	constructor(filter: HttpFilter, action: KFunction<*>) : this(filter) {
		action(action)
	}

	fun action(action: KFunction<*>): WebPath {
		val c = action.ownerClass!!
		list.add(c.pageName)
		list.add(action.actionName)
		this.action = action
		return this
	}



	//参数名是action函数的参数的名字,
	fun param(v: Any?): WebPath {
		return params(v)
	}

	fun paramsList(ps: List<Any?>): WebPath {
		val ac = action ?: throw IllegalArgumentException("没有提供函数!")
		val ls = ac.valueParameters
		val m = min(ps.size, ls.size)
		for (i in 0 until m) {
			val k = ls[i].userName
			val v = ps[i]
			arg(k, v)
		}
		return this
	}

	fun params(vararg paramArray: Any?): WebPath {
		return paramsList(listOf(*paramArray))
	}

	fun arg(p: KProperty0<*>): WebPath {
		arg(p.userName, p.getValue()?.toString() ?: "")
		return this
	}

	fun arg(p: Prop1, value: Any?): WebPath {
		if (value != null) {
			params[p.userName] = value.toString()
		}
		return this
	}

	fun arg(key: String, value: Any?): WebPath {
		if (value != null) {
			params[key] = value.toString()
		}
		return this
	}

	fun append(resPath: String): WebPath {
		list.add(resPath)
		return this
	}



	// /app_path
	val uriApp: String
		get() {
			var s = buildPath(filter.patternPath, list.joinToString("/"))
			if (params.isNotEmpty()) {
				val argStr = params.filter { !it.value.isNullOrEmpty() }.map { it.key + "=" + URLEncoder.encode(it.value, Charsets.UTF_8.name()) }.joinToString("&")
				s = "$s?$argStr"
			}
			return s
		}

	// /appname/app_path
	val uri: String
		get() {
			var s = buildPath(filter.contextPath, filter.patternPath, list.joinToString("/"))
			if (params.isNotEmpty()) {
				val argStr = params.filter { !it.value.isNullOrEmpty() }.map { it.key + "=" + URLEncoder.encode(it.value, Charsets.UTF_8.name()) }.joinToString("&")
				s = "$s?$argStr"
			}
			return s
		}

	val uriRoot: String
		get() {
			return buildPath(filter.contextPath)
		}

	fun fullUrlOf(req: HttpServletRequest): String {
		return "http://" + req.getHeader("host") + this.uri
	}

	//uriRes("css/bootstrap.css") => /contextPath/css/bootstrap.css
	fun uriRes(p: String): String {
		return buildPath(filter.contextPath, p)
	}

	companion object {

		fun buildPath(vararg ps: String): String {
			val sb = StringBuilder(128)
			for (s in ps) {
				if (s.isNotEmpty()) {
					if (s.startsWith("/")) {
						sb.append(s)
					} else {
						sb.append('/').append(s)
					}
				}
			}
			return sb.toString()
		}
	}

}

fun WebPath.ok(msg: String): WebPath {
	return arg(ParamConst.SUCCESS, msg)
}

fun WebPath.err(msg: String): WebPath {
	return arg(ParamConst.ERROR, msg)
}

fun WebPath.success(msg: String): WebPath {
	return arg(ParamConst.SUCCESS, msg)
}

fun WebPath.error(msg: String): WebPath {
	return arg(ParamConst.ERROR, msg)
}