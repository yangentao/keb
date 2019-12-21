package dev.entao.kava.sql

import dev.entao.kava.base.*
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import dev.entao.kava.json.YsonParser
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

	fun hasProp(p: Prop): Boolean {
		return this.containsKey(p.sqlName) || this.containsKey(p.userName)
	}

	fun removeProperty(p: KProperty<*>) {
		this.remove(p.sqlName)
		this.remove(p.userName)
	}

	operator fun get(prop: KProperty<*>): Any? {
		return this.getValue(this, prop)
	}

	operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
		this[property.sqlName] = value
		if (this.gather) {
			if (property is KMutableProperty) {
				if (property !in this._changedProperties) {
					this._changedProperties.add(property)
				}
			}
		}
	}

	//select user.id, user.name from user
	//sqlite 返回的结果集中, columnLabel 是 user.id, user.name
	//mysql 返回的结果集 columnLabel 是 id, name
	@Suppress("UNCHECKED_CAST")
	operator fun <V> getValue(thisRef: Any?, property: KProperty<*>): V {
		val retType = property.returnType
//		val v = this[property.sqlFullName] ?: this[property.sqlName]
		val v = this[property.sqlName] ?: this[property.userName]
		if (v != null) {
			if (retType.isClass(YsonObject::class)) {
				return when (v) {
					is String -> YsonObject(v) as V
					is YsonObject -> v as V
					else -> {
						return YsonParser(v.toString()).parseObject() as V
					}
				}
			} else if (retType.isClass(YsonArray::class)) {
				return when (v) {
					is String -> YsonArray(v) as V
					is YsonArray -> v as V
					else -> {
						return YsonParser(v.toString()).parseArray() as V
					}
				}
			}
			//TODO decimal, double, float, Long, Int
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