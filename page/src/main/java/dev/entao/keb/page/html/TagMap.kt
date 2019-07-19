package dev.entao.keb.page.html

import dev.entao.kava.base.isTypeBoolean
import dev.entao.kava.base.isTypeInt
import dev.entao.kava.base.isTypeString
import dev.entao.kava.base.userName
import java.util.HashMap
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class TagMap : HashMap<String, String>(16) {

	fun removeProperty(p: KProperty<*>) {
		this.remove(p.userName)
	}

	operator fun <T> setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		val k = property.userName
		when (value) {
			is String -> put(k, value)
			is Boolean -> {
				if (value) {
					put(k, k)
				} else {
					remove(k)
				}
			}
			else -> put(k, value.toString())
		}
	}

	operator fun <T> getValue(thisRef: Any?, property: KProperty<*>): T {
		val pname = property.userName
		val rt = property.returnType
		val v = this[pname] ?: ""
		if (rt.isTypeString) {
			return v as T
		}
		if (rt.isTypeBoolean) {
			return (v == pname) as T
		}
		if (rt.isTypeInt) {
			if (v.isEmpty()) {
				return 0 as T
			} else {
				return v.toInt() as T
			}
		}
		throw IllegalArgumentException("不支持的类型$property")
	}

}