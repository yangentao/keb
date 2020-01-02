@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.log.logd
import dev.entao.kava.base.closeSafe
import dev.entao.json.YsonArray
import dev.entao.json.YsonObject
import dev.entao.json.YsonValue
import java.sql.ResultSet
import java.sql.ResultSetMetaData


//=============each=================
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
			val yo = it.rowToJsonObject(meta)
			block(yo)
		}
	}
}

fun ResultSet.eachMap(block: (ModelMap) -> Unit) {
	val meta = this.metaData
	this.closeAfter {
		while (this.next()) {
			val m = it.rowToMap(meta)
			block(m)
		}
	}
}

//=============first=================

inline fun <R> ResultSet.firstRow(block: (ResultSet) -> R?): R? {
	this.closeAfter {
		if (it.next()) {
			return block(it)
		}
	}
	return null
}

val ResultSet.existRow: Boolean
	get() {
		this.closeAfter {
			return this.next()
		}
	}

val ResultSet.firstMap: ModelMap?
	get() {
		this.closeAfter {
			if (this.next()) {
				return it.rowToMap(it.metaData)
			}
		}
		return null
	}

val ResultSet.firstObject: YsonObject?
	get() {
		this.closeAfter {
			if (this.next()) {
				return it.rowToJsonObject(it.metaData)
			}
		}
		return null
	}
//=============list=================

inline fun <R> ResultSet.allRows(block: (ResultSet) -> R?): ArrayList<R> {
	val ls = ArrayList<R>(256)
	this.closeAfter {
		while (it.next()) {
			ls += block(it) ?: continue
		}
	}
	return ls
}

fun <T : Model> ResultSet.allModels(block: () -> T): List<T> {
	return this.allMaps.map {
		val m = block()
		m.model.putAll(it)
		m
	}
}

val ResultSet.allMaps: ArrayList<ModelMap>
	get() {
		val list = ArrayList<ModelMap>(128)
		val meta = this.metaData
		this.closeAfter {
			while (this.next()) {
				list += it.rowToMap(meta)
			}
		}
		return list
	}


val ResultSet.allJson: YsonArray
	get() {
		val arr = YsonArray(256)
		val meta = this.metaData
		this.closeAfter {
			while (this.next()) {
				arr.data += it.rowToJsonObject(meta)
			}
		}
		return arr
	}
val ResultSet.allObjects: ArrayList<YsonObject>
	get() {
		val arr = ArrayList<YsonObject>(256)
		val meta = this.metaData
		this.closeAfter {
			while (this.next()) {
				arr += it.rowToJsonObject(meta)
			}
		}
		return arr
	}
//==============utils==============

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

fun ResultSet.getJson(col: Int): YsonValue? {
	val js = this.getString(col) ?: return null
	return if (js.startsWith("{")) {
		YsonObject(js)
	} else if (js.startsWith("[")) {
		YsonArray(js)
	} else {
		null
	}
}

fun ResultSet.getJsonObject(col: Int): YsonObject? {
	val js = this.getString(col) ?: return null
	return if (js.startsWith("{")) {
		YsonObject(js)
	} else {
		null
	}
}

fun ResultSet.getJsonArray(col: Int): YsonArray? {
	val js = this.getString(col) ?: return null
	return if (js.startsWith("[")) {
		YsonArray(js)
	} else {
		null
	}
}


fun ResultSet.rowToMap(meta: ResultSetMetaData): ModelMap {
	val map = ModelMap()
	for (i in 1..meta.columnCount) {
		val label = meta.getColumnLabel(i)
		val value = this.getObject(i)
		map[label] = value
	}
	return map
}

fun ResultSet.rowToJsonObject(meta: ResultSetMetaData): YsonObject {
	val yo = YsonObject(meta.columnCount + 2)
	for (i in 1..meta.columnCount) {
		val label = meta.getColumnLabel(i)
		val typeName = meta.getColumnTypeName(i)
		val value: Any? = if (typeName in jsonTypes) {
			val js = this.getString(i)?.trim()
			if (js == null || js.isEmpty()) {
				null
			} else if (js.startsWith("{")) {
				YsonObject(js)
			} else if (js.startsWith("[")) {
				YsonArray(js)
			} else {
				null
			}
		} else {
			this.getObject(i)
		}
		yo.any(label, value)
	}
	return yo
}

fun ResultSet.closeWithStatement() {
	val st = this.statement
	this.closeSafe()
	st?.closeSafe()
}

private val jsonTypes: Set<String> = setOf("json", "JSON", "jsonb", "JSONB")


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