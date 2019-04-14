package dev.entao.ken.ex

import dev.entao.kbase.*
import dev.entao.sql.Model
import dev.entao.sql.modelProperties
import javax.servlet.http.HttpServletRequest

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