package dev.entao.kava.json

import dev.entao.kava.base.defaultValue
import dev.entao.kava.base.userName
import dev.entao.kava.base.ITextConvert
import dev.entao.kava.base.TextConverts
import dev.entao.kava.base.defaultValueOfProperty
import dev.entao.kava.base.strToV
import kotlin.reflect.KProperty

class YsonObject(val data: LinkedHashMap<String, YsonValue> = LinkedHashMap(32)) : YsonValue(), MutableMap<String, YsonValue> by data {

	constructor(capcity: Int) : this(LinkedHashMap<String, YsonValue>(capcity))

	constructor(json: String) : this() {
		val v = YsonParser(json).parse(true)
		if (v is YsonObject) {
			data.putAll(v.data)
		}
	}

	override fun yson(buf: StringBuilder) {
		buf.append("{")
		var first = true
		for ((k, v) in data) {
			if (!first) {
				buf.append(",")
			}
			first = false
			buf.append("\"").append(escapeJson(k)).append("\":")
			v.yson(buf)
		}
		buf.append("}")
	}

	override fun preferBufferSize(): Int {
		return 256
	}

	override fun toString(): String {
		return yson()
	}

	operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
		this.put(property.userName, Yson.toYson(value))
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <V> getValue(thisRef: Any?, property: KProperty<*>): V {
		val retType = property.returnType
		val v = this.get(property.userName) ?: YsonNull.inst
		if (v !is YsonNull) {
			val pv = YsonDecoder.decodeByType(v, retType, null)
			return pv as V
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

	fun str(key: String, value: String?) {
		if (value == null) {
			put(key, YsonNull.inst)
		} else {
			put(key, YsonString(value))
		}
	}

	fun str(key: String): String? {
		val a = get(key) as? YsonString
		return a?.data
	}

	fun int(key: String, value: Int?) {
		if (value == null) {
			put(key, YsonNull.inst)
		} else {
			put(key, YsonNum(value))
		}
	}

	fun int(key: String): Int? {
		val a = get(key) as? YsonNum
		return a?.data?.toInt()
	}

	fun long(key: String, value: Long?) {
		if (value == null) {
			put(key, YsonNull.inst)
		} else {
			put(key, YsonNum(value))
		}
	}

	fun long(key: String): Long? {
		val a = get(key) as? YsonNum
		return a?.data?.toLong()
	}

	fun real(key: String, value: Double?) {
		if (value == null) {
			put(key, YsonNull.inst)
		} else {
			put(key, YsonNum(value))
		}
	}

	fun real(key: String): Double? {
		val a = get(key) as? YsonNum
		return a?.data?.toDouble()
	}

	fun bool(key: String, value: Boolean?) {
		if (value == null) {
			put(key, YsonNull.inst)
		} else {
			put(key, YsonBool(value))
		}
	}

	fun bool(key: String): Boolean? {
		val a = get(key) as? YsonBool
		return a?.data
	}

	fun obj(key: String, value: YsonObject?) {
		if (value == null) {
			put(key, YsonNull.inst)
		} else {
			put(key, value)
		}
	}

	fun obj(key: String): YsonObject? {
		return get(key) as? YsonObject
	}

	fun arr(key: String, value: YsonArray?) {
		if (value == null) {
			put(key, YsonNull.inst)
		} else {
			put(key, value)
		}
	}

	fun arr(key: String): YsonArray? {
		return get(key) as? YsonArray
	}

	fun any(key: String, value: Any?) {
		put(key, from(value))
	}

	fun any(key: String): Any? {
		return get(key)
	}


	fun addObject(key: String): YsonObject {
		val yo = YsonObject()
		this.obj(key, yo)
		return yo
	}

	fun addArray(key: String): YsonArray {
		val yarr = YsonArray()
		this.arr(key, yarr)
		return yarr
	}

	fun obj(key: String, block: YsonObject.() -> Unit) {
		val b = YsonObject()
		this.obj(key, b)
		b.block()
	}

	infix fun <V> String.TO(value: V) {
		any(this, value)
	}

	companion object {
		init {

			TextConverts[YsonObject::class] = YsonObjectText
		}
	}
}

object YsonObjectText : ITextConvert {
	override val defaultValue: Any = YsonObject()
	override fun fromText(text: String): Any? {
		return YsonObject(text)
	}
}


fun ysonObject(block: YsonObject.() -> Unit): YsonObject {
	val b = YsonObject()
	b.block()
	return b
}


