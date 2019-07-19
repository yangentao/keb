package dev.entao.keb.core.sql

import dev.entao.kava.base.*
import dev.entao.kava.sql.*
import dev.entao.keb.core.*
import javax.servlet.http.HttpServletRequest

fun HttpGroup.EQ(vararg ps: Prop1): Where? {
	var w: Where? = null
	for (p in ps) {
		val v = paramValue(p) ?: continue
		w = w AND p.sqlFullName.EQ(v)
	}
	return w
}

// %value%
fun HttpGroup.LIKE(p: Prop1): Where? {
	val v = httpParams.str(p)?.trim() ?: return null
	if (v.isEmpty()) {
		return null
	}
	return p LIKE """%$v%"""
}

// value%
fun HttpGroup.LIKE_(p: Prop1): Where? {
	val v = httpParams.str(p)?.trim() ?: return null
	if (v.isEmpty()) {
		return null
	}
	return p LIKE """$v%"""
}

// %value
fun HttpGroup._LIKE(p: Prop1): Where? {
	val v = httpParams.str(p)?.trim() ?: return null
	if (v.isEmpty()) {
		return null
	}
	return p LIKE """%$v"""
}

fun HttpGroup.NE(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p NE v
}

fun HttpGroup.GE(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p GE v
}

fun HttpGroup.GT(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p GT v
}

fun HttpGroup.LE(p: Prop1): Where? {
	val v = paramValue(p) ?: return null
	return p LE v
}

fun HttpGroup.LT(p: Prop1): Where? {
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