package dev.entao.keb.core

import dev.entao.kava.base.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions

/**
 * Created by entaoyang@163.com on 2017/5/7.
 */



fun Prop1.valOf(item: Any): String {
	return this.getValue(item)?.toString() ?: ""
}

val KClass<*>.actionList: List<KFunction<*>>
	get() {
		return this.memberFunctions.filter { it.isHttpAction }
	}

//fun loginAction()
val KFunction<*>.isHttpAction: Boolean
	get() {
		if (!this.name.endsWith(HttpFilter.ACTION)) {
			return false
		}
		if (this.hasAnnotation<Exclude>()) {
			return false
		}
		if (this.visibility != KVisibility.PUBLIC) {
			return false
		}
		return true
	}

val KFunction<*>.actionName: String
	get() {
		val fname = this.userName.substringBefore(HttpFilter.ACTION).toLowerCase()
		return if (fname == HttpFilter.INDEX) "" else fname
	}
val KClass<*>.pageName: String
	get() {
		val gname = this.userName.substringBefore(HttpFilter.GROUP_SUFFIX).toLowerCase()
		if (gname == HttpFilter.INDEX) {
			return ""
		}
		return gname
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