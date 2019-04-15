package dev.entao.keb.core

import dev.entao.kava.base.*
import dev.entao.keb.core.anno.NavItem
import java.net.URLEncoder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

/**
 * Created by entaoyang@163.com on 2017/5/7.
 */



typealias HttpAction = KFunction<Unit>

fun Prop1.pairOf(item: Any): Pair<String, String> {
	return this.userName to (this.getValue(item)?.toString() ?: "")
}

infix fun Prop1.OF(item: Any): Pair<String, String> {
	return this.userName to (this.getValue(item)?.toString() ?: "")
}

infix fun Prop1.VAL(item: Any): String {
	return this.getValue(item)?.toString() ?: ""
}

fun Prop1.valOf(item: Any): String {
	return this.getValue(item)?.toString() ?: ""
}

val KClass<*>.actionList: List<KFunction<*>>
	get() {
		return this.memberFunctions.filter { it.isHttpAction }
	}
val KClass<*>.actionListWithNavItem: List<KFunction<*>>
	get() {
		return this.actionList.filter { it.hasAnnotation<NavItem>() }
	}

//fun loginAction()
val KFunction<*>.isHttpAction: Boolean
	get() {
		if (!this.name.endsWith(HttpFilter.ACTION)) {
			return false
		}
		if (this.hasAnnotation<dev.entao.kava.sql.Exclude>()) {
			return false
		}
		if (this.visibility != KVisibility.PUBLIC) {
			return false
		}
		return true
	}

val KFunction<*>.actionName: String
	get() {
		val funName = this.userName
		val fname = funName.substringBefore(HttpFilter.ACTION).toLowerCase()
		return if (fname == HttpFilter.INDEX) "" else fname
	}
val KClass<*>.pageName: String
	get() {
		val nameValue = this.findAnnotation<Name>()?.value
		if (nameValue != null) {
			return nameValue.toLowerCase()
		}
		val simName = this.simpleName ?: throw IllegalArgumentException("Page类必须有类名")
		val groupName = if (simName.endsWith(HttpFilter.PAGE)) {
			simName.substringBefore(HttpFilter.PAGE).toLowerCase()
		} else if (simName.endsWith(HttpFilter.API_PAGE)) {
			"api/" + simName.substringBefore(HttpFilter.API_PAGE).toLowerCase()
		} else {
			simName.toLowerCase()
		}
		if (groupName == HttpFilter.INDEX) {
			return ""
		}
		return groupName
	}

fun urlBuild(url: String, params: Map<String, String?>): String {
	if (params.isEmpty()) {
		return url
	}
	val argStr = params.filter { !it.value.isNullOrEmpty() }.map { it.key + "=" + URLEncoder.encode(it.value, Charsets.UTF_8.name()) }.joinToString("&")
	if ('?' !in url) {
		return url + "?" + argStr
	}
	if (url.last() == '&') {
		return url + argStr
	}
	return url + "&" + argStr
}

//	/ =>
//  /* =>
//  /person => person
//  /person/ => person
//  /person/* => person
fun formatPatternPath(s: String): String {
	val sb = StringBuilder(36)
	var first = true
	for (ch in s) {
		if (first) {
			if (ch == '/') {
				continue
			}
		}
		if (ch in '0'..'9' || ch in 'a'..'z' || ch in 'A'..'Z' || ch == '_') {
			sb.append(ch)
			first = false
			continue
		}
		if (!first) {
			break
		}

	}
	return sb.toString()
}

val String.intList: List<Int>
	get() {
		return this.split(',').map { it.toInt() }
	}

fun isSubpath(longPath: String, shortPath: String): Boolean {
	val uu = "$longPath/"
	return if (shortPath.endsWith("/")) {
		uu.startsWith(shortPath)
	} else {
		uu.startsWith("$shortPath/")
	}
}