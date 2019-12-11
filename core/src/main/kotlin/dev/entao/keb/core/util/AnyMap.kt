package dev.entao.keb.core.util

import dev.entao.kava.base.defaultValue
import dev.entao.kava.base.defaultValueOfProperty
import dev.entao.kava.base.strToV
import dev.entao.kava.base.userName
import kotlin.reflect.KProperty

class AnyMap(capacity: Int = 32) : HashMap<String, Any?>(capacity) {


	fun removeProperty(p: KProperty<*>) {
		this.remove(p.userName)
	}

	operator fun get(prop: KProperty<*>): Any? {
		return this.getValue(this, prop)
	}

	operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
		this[property.userName] = value

	}

	@Suppress("UNCHECKED_CAST")
	operator fun <V> getValue(thisRef: Any?, property: KProperty<*>): V {
		val retType = property.returnType
		val v = this[property.userName]
		if (v != null || retType.isMarkedNullable) {
			return v as V
		}
		val defVal = property.defaultValue
		if (defVal != null) {
			return strToV(defVal, property)
		}
		return defaultValueOfProperty(property)
	}

}