@file:Suppress("FunctionName", "unused")

package dev.entao.keb.page

import dev.entao.kava.base.*
import dev.entao.kava.sql.*
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpScope
import dev.entao.keb.core.param
import dev.entao.keb.core.paramNameSet
import javax.servlet.http.HttpServletRequest

fun SQLQuery.limitPage(context: HttpContext) {
	val n = context.httpParams.int(P.pageArg) ?: 0
	this.limit(P.pageSize, n * P.pageSize)
}

class SortParam(context: HttpContext, sortByName: String, desc: Boolean = true) {

	val sortBy: String
	val desc: Boolean

	init {
		val a = context.httpParams.str(P.sortBy)
		val d = context.httpParams.str(P.sortDesc) == "1"
		if (a != null) {
			this.sortBy = a
			this.desc = d
		} else {
			this.sortBy = sortByName
			this.desc = desc
		}
	}

	constructor(context: HttpContext, p: Prop1, desc: Boolean) : this(context, p.userName, desc)
}


fun SQLQuery.orderBy(sp: SortParam) {
	if (sp.sortBy.isNotEmpty()) {
		if (sp.desc) {
			desc(sp.sortBy)
		} else {
			asc(sp.sortBy)
		}
	}
}

private fun HttpScope.sqlParam(p: Prop1): Any? {
	if (p.isTypeInt) {
		return context.httpParams.int(p)
	}
	if (p.isTypeLong) {
		return context.httpParams.long(p)
	}
	if (p.isTypeFloat || p.isTypeDouble) {
		return context.httpParams.double(p)
	}
	if (p.isTypeString) {
		val s = context.httpParams.str(p) ?: return null
		if (s.isEmpty()) {
			return null
		}
	}
	return null
}

fun HttpScope.EQ(vararg ps: Prop1): Where? {
	var w: Where? = null
	for (p in ps) {
		val v = sqlParam(p) ?: continue
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
	val v = sqlParam(p) ?: return null
	return p NE v
}

fun HttpScope.GE(p: Prop1): Where? {
	val v = sqlParam(p) ?: return null
	return p GE v
}

fun HttpScope.GT(p: Prop1): Where? {
	val v = sqlParam(p) ?: return null
	return p GT v
}

fun HttpScope.LE(p: Prop1): Where? {
	val v = sqlParam(p) ?: return null
	return p LE v
}

fun HttpScope.LT(p: Prop1): Where? {
	val v = sqlParam(p) ?: return null
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


