package dev.entao.kava.sql

import dev.entao.kava.base.defaultValue
import dev.entao.kava.base.fullName
import dev.entao.kava.base.userName
import dev.entao.kava.base.defaultValueOfProperty
import dev.entao.kava.base.isClass
import dev.entao.kava.base.strToV
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2017/4/20.
 */

class ModelMap(capacity: Int = 32) : HashMap<String, Any?>(capacity) {

	private val _changedProperties = ArrayList<KMutableProperty<*>>(8)
	private var gather: Boolean = false

	@Synchronized
	fun gather(block: () -> Unit): ArrayList<KMutableProperty<*>> {
		this.gather = true
		this._changedProperties.clear()
		block()
		val ls = ArrayList<KMutableProperty<*>>(_changedProperties)
		this.gather = false
		return ls
	}

	fun removeProperty(p: KProperty<*>) {
		this.remove(p.userName)
	}

	operator fun get(prop: KProperty<*>): Any? {
		return this.getValue(this, prop)
	}

	operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
		this[property.userName] = value
		if (this.gather) {
			if (property is KMutableProperty) {
				if (property !in this._changedProperties) {
					this._changedProperties.add(property)
				}
			}
		}
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <V> getValue(thisRef: Any?, property: KProperty<*>): V {
		val retType = property.returnType
		val v = this[property.sqlFullName] ?: this[property.userName]
		if (v != null) {
			if (retType.isClass(YsonObject::class)) {
				return when (v) {
					is String -> YsonObject(v) as V
					is YsonObject -> v as V
					else -> throw IllegalArgumentException("类型不匹配: " + property.fullName)
				}
			} else if (retType.isClass(YsonArray::class)) {
				return when (v) {
					is String -> YsonArray(v) as V
					is YsonArray -> v as V
					else -> throw IllegalArgumentException("类型不匹配: " + property.fullName)
				}
			}

			return v as V
		}
		if (retType.isMarkedNullable) {
			return null as V
		}
		val defVal = property.defaultValue
		if (defVal != null) {
			return strToV(defVal, property)
		}
		return defaultValueOfProperty(property)
	}

}