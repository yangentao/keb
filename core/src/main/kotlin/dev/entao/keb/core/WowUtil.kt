package dev.entao.keb.core

import dev.entao.kava.base.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/5/7.
 */

fun Prop1.valOf(item: Any): String {
	return this.getValue(item)?.toString() ?: ""
}

//不支持继承, 不支持扩展函数
//不能列举所有的扩展函数,  declaredMemberExtensionFunctions 只返回在该类内部定义的扩展函数.
//继承, 父类函数的ownerClass 返回的是父类, 类名与子类不同, 生成的uri也不同 , 这容易造成问题
val KClass<*>.actionList: List<KFunction<*>>
	get() {
		return this.declaredMemberFunctions.filter { it.isHttpAction }
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
		return pageClassMap.getOrPut(this) {
			makePageName(this)
		}
	}

private fun makePageName(cls: KClass<*>): String {
	val named = cls.findAnnotation<Name>()?.value
	if (named != null) {
		return named
	}
	val clsName = cls.simpleName!!
	for (ps in HttpFilter.pageSuffixs) {
		if (clsName != ps && clsName.endsWith(ps)) {
			val gname = clsName.substr(0, clsName.length - ps.length)
			if (gname == HttpFilter.INDEX) {
				return ""
			} else {
				return gname
			}
		}
	}
	return clsName
}

private val pageClassMap = HashMap<KClass<*>, String>()

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
	return sb.toString().toLowerCase()
}