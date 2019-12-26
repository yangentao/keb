@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.log.logd
import dev.entao.kava.base.closeSafe
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import java.lang.IllegalArgumentException
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Types

inline fun <R> ResultSet.closeAfter(block: (ResultSet) -> R): R {
	var closed = false
	try {
		return block(this)
	} catch (e: Exception) {
		closed = true
		try {
			this.closeWithStatement()
		} catch (closeException: Exception) {
		}
		throw e
	} finally {
		if (!closed) {
			this.closeWithStatement()
		}
	}
}

fun ResultSet.closeWithStatement() {
	val st = this.statement
	this.closeSafe()
	st?.closeSafe()
}

fun ResultSet.dump() {
	val meta = this.metaData
	val sb = StringBuilder(512)
	this.closeAfter {
		while (this.next()) {
			sb.setLength(0)
			for (i in 1..meta.columnCount) {
				val label = meta.getColumnLabel(i)
				val value = this.getObject(i)
				sb.append(label).append("=").append(value).append(", ")
			}
			logd(sb.toString())
		}
	}
}

fun <T : Model> ResultSet.models(block: () -> T): List<T> {
	return this.allRows().map {
		val m = block()
		m.model.putAll(it)
		m
	}
}

val ResultSet.existRow: Boolean
	get() {
		this.closeAfter {
			return this.next()
		}
	}

val ResultSet.intList: ArrayList<Int>
	get() {
		val ls = ArrayList<Int>()
		this.closeAfter {
			while (it.next()) {
				ls += it.getInt(1)
			}
		}
		return ls
	}

val ResultSet.longList: ArrayList<Long>
	get() {
		val ls = ArrayList<Long>()
		this.closeAfter {
			while (it.next()) {
				ls += it.getLong(1)
			}
		}
		return ls
	}
val ResultSet.strList: ArrayList<String>
	get() {
		val ls = ArrayList<String>()
		this.closeAfter {
			while (it.next()) {
				ls += it.getString(1)
			}
		}
		return ls
	}

val ResultSet.doubleList: ArrayList<Double>
	get() {
		val ls = ArrayList<Double>()
		this.closeAfter {
			while (it.next()) {
				ls += it.getDouble(1)
			}
		}
		return ls
	}

val ResultSet.intValue: Int?
	get() {
		this.closeAfter {
			if (it.next()) {
				val t = this.metaData.getColumnType(1)
				return when (t) {
					Types.BIGINT -> it.getLong(1).toInt()
					Types.INTEGER, Types.SMALLINT, Types.TINYINT -> it.getInt(1)
					else -> null
				}
			}
		}
		return null
	}
val ResultSet.longValue: Long?
	get() {
		this.closeAfter {
			if (it.next()) {
				val t = this.metaData.getColumnType(1)
				return when (t) {
					Types.BIGINT -> it.getLong(1)
					Types.INTEGER, Types.SMALLINT, Types.TINYINT -> it.getInt(1).toLong()
					else -> null
				}
			}
		}
		return null
	}

val ResultSet.strValue: String?
	get() {
		this.closeAfter {
			if (it.next()) {
				return it.getString(1)
			}
		}
		return null
	}
val ResultSet.doubleValue: Double?
	get() {
		this.closeAfter {
			if (it.next()) {
				return it.getDouble(1)
			}
		}
		return null
	}
val ResultSet.anyValue: Any?
	get() {
		this.closeAfter {
			if (it.next()) {
				return it.getObject(1)
			}
		}
		return null
	}

fun ResultSet.firstRow(): ModelMap? {
	this.closeAfter {
		if (this.next()) {
			return resultModelMap(it, it.metaData)
		}
	}
	return null
}

//todo rename to allRows
fun ResultSet.allRows(): ArrayList<ModelMap> {
	val list = ArrayList<ModelMap>(128)
	val meta = this.metaData
	this.closeAfter {
		while (this.next()) {
			list += resultModelMap(it, meta)
		}
	}
	return list
}

private val jsonTypes: Set<String> = setOf("json", "JSON", "jsonb", "JSONB")
fun ResultSet.ysonArray(): YsonArray {
	val arr = YsonArray(256)
	val meta = this.metaData
	this.closeAfter {
		while (this.next()) {
			arr += resultObject(it, meta)
		}
	}
	return arr
}

fun ResultSet.firstObject(): YsonObject? {
	this.closeAfter {
		if (this.next()) {
			return resultObject(it, it.metaData)
		}
	}
	return null
}

fun ResultSet.eachRow(block: (ResultSet) -> Unit) {
	this.closeAfter {
		while (this.next()) {
			block(it)
		}
	}
}

fun ResultSet.eachObject(block: (YsonObject) -> Unit) {
	val meta = this.metaData
	this.closeAfter {
		while (this.next()) {
			val yo = resultObject(it, meta)
			block(yo)
		}
	}
}

fun ResultSet.eachModelMap(block: (ModelMap) -> Unit) {
	val meta = this.metaData
	this.closeAfter {
		while (this.next()) {
			val m = resultModelMap(it, meta)
			block(m)
		}
	}
}

private fun resultModelMap(rs: ResultSet, meta: ResultSetMetaData): ModelMap {
	val map = ModelMap()
	for (i in 1..meta.columnCount) {
		val label = meta.getColumnLabel(i)
		val value = rs.getObject(i)
		map[label] = value
	}
	return map
}

private fun resultObject(rs: ResultSet, meta: ResultSetMetaData): YsonObject {
	val yo = YsonObject(meta.columnCount + 2)
	for (i in 1..meta.columnCount) {
		val label = meta.getColumnLabel(i)
		val typeName = meta.getColumnTypeName(i)
		val value: Any? = if (typeName in jsonTypes) {
			val js = rs.getString(i)?.trim()
			if (js == null || js.isEmpty()) {
				null
			} else if (js.startsWith("{")) {
				YsonObject(js)
			} else if (js.startsWith("[")) {
				YsonArray(js)
			} else {
				throw  IllegalArgumentException("json格式非法: $js ")
			}
		} else {
			rs.getObject(i)
		}
		yo.any(label, value)
	}
	return yo
}