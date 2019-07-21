@file:Suppress("FunctionName", "unused")

package dev.entao.keb.page.ex

import dev.entao.kava.base.*
import dev.entao.kava.sql.*
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpScope
import dev.entao.keb.core.param
import dev.entao.keb.core.paramNameSet
import dev.entao.keb.page.P
import javax.servlet.http.HttpServletRequest

fun SQLQuery.limitPage(context: HttpContext) {
	val n = context.httpParams.int(P.pageN) ?: 0
	this.limit(P.pageSize, n * P.pageSize)
}

class OrderParam(val context: HttpContext, p: Prop1, desc: Boolean = true) {

	val sortBy: String
	val desc: Boolean

	init {
		val ascKey = context.httpParams.str(P.ascKey)
		val descKey = context.httpParams.str(P.descKey)

		this.sortBy = ascKey ?: descKey ?: p.userName
		this.desc = if (ascKey == null && descKey == null) {
			desc
		} else {
			descKey != null
		}

	}
}

fun HttpScope.OrderBy(p: Prop1, desc: Boolean = true): OrderParam {
	return OrderParam(context, p, desc)
}

fun SQLQuery.orderBy(sp: OrderParam) {
	if (sp.sortBy.isNotEmpty()) {
		if (sp.desc) {
			desc(sp.sortBy)
		} else {
			asc(sp.sortBy)
		}
	}
}

fun HttpScope.EQ(vararg ps: Prop1): Where? {
	var w: Where? = null
	for (p in ps) {
		val v = paramValue(p) ?: continue
		w = w AND p.sqlFullName.EQ(v)
	}
	return w
}

// %value%
fun HttpScope.LIKE(p: Prop1): Where? {
	val v = httpParams.str(p)?.trim() ?: return null
	if (v.isEmpty()) {
		return null
	}
	return p LIKE """%$v%"""
}

// value%
fun HttpScope.LIKE_(p: Prop1): Where? {
	val v = httpParams.str(p)?.trim() ?: return null
	if (v.isEmpty()) {
		return null
	}
	return p LIKE """$v%"""
}

// %value
fun HttpScope._LIKE(p: Prop1): Where? {
	val v = httpParams.str(p)?.trim() ?: return null
	if (v.isEmpty()) {
		return null
	}
	return p LIKE """%$v"""
}

fun HttpScope.NE(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p NE v
}

fun HttpScope.GE(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p GE v
}

fun HttpScope.GT(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p GT v
}

fun HttpScope.LE(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p LE v
}

fun HttpScope.LT(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p LT v
}

fun Model.fromRequest(context: HttpContext) {
	this.fromRequest(context.request)
}

fun Model.fromRequest(req: HttpServletRequest) {
	val nameSet = req.paramNameSet
	val thisInst = this
	this::class.modelProperties.forEach {
		val key = it.userName
		if (key in nameSet) {
			val sval = req.param(key)
			if (sval != null) {
				val v: Any? = if (it.hasAnnotation<Trim>()) {
					strToV(sval.trim(), it)
				} else {
					strToV(sval, it)
				}
				if (v != null || it.returnType.isMarkedNullable) {
					it.setValue(thisInst, v)
				} else {
					it.setValue(thisInst, defaultValueOfProperty(it))
				}
			}
		}
	}
}


