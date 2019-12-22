package dev.entao.kava.sql

import dev.entao.kava.base.*
import dev.entao.kava.log.logd
import java.sql.Connection
import kotlin.reflect.KClass

fun Connection.insert(model: Model): Boolean {
	val kvs = model.modelPropertiesExists.map { it to it.getValue(model) }
	val autoInc = model::class.modelPrimaryKeys.find { it.hasAnnotation<AutoInc>() } != null
	if (!autoInc) {
		return this.insert(model::class, kvs)
	}
	val r = this.insertGenKey(model::class, kvs)
	if (r <= 0L) {
		return false
	}
	val pkProp = model::class.modelPrimaryKeys.first { it.hasAnnotation<AutoInc>() }
	if (pkProp.returnType.isTypeLong) {
		pkProp.setValue(model, r)
	} else {
		pkProp.setValue(model, r.toInt())
	}
	return true
}

fun Connection.insert(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>): Boolean {
	return this.insert(modelCls.sqlName, kvs.map { it.first.sqlName to it.second })
}

fun Connection.insert(table: String, kvs: List<Pair<String, Any?>>): Boolean {
	val ks = kvs.joinToString(", ") { it.first }
	val vs = kvs.joinToString(", ") { valPos(it.second) }
	val sql = "INSERT INTO $table ($ks) VALUES ($vs) "
	val args = kvs.map { it.second }
	return this.update(sql, args) > 0
}

fun Connection.insertGenKey(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>): Long {
	return this.insertGenKey(modelCls.sqlName, kvs.map { it.first.sqlName to it.second })
}

fun Connection.insertGenKey(table: String, kvs: List<Pair<String, Any?>>): Long {
	val ks = kvs.joinToString(", ") { it.first }
	val vs = kvs.joinToString(", ") { valPos(it.second) }
	val sql = "INSERT INTO $table ($ks) VALUES ($vs) "
	val args = kvs.map { it.second }
	return this.insertGen(sql, args)
}

fun Connection.insertGen(sql: String, args: List<Any?>): Long {
	val st = this.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
	st.setParams(args)
	if (ConnLook.logEnable) {
		logd(sql)
		logd(args)
	}
	st.use {
		val n = it.executeUpdate()
		return if (n <= 0) {
			0L
		} else {
			it.generatedKeys.longValue ?: 0L
		}
	}
}

fun Connection.insertOrUpdate(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>, uniqColumns: List<Prop>): Boolean {
	return this.insertOrUpdate(modelCls.sqlName, kvs.map { it.first.sqlName to it.second }, uniqColumns.map { it.sqlName })
}

fun Connection.insertOrUpdate(table: String, kvs: List<Pair<String, Any?>>, uniqColumns: List<String>): Boolean {
	if (uniqColumns.isEmpty()) {
		throw IllegalArgumentException("insertOrUpdate $table  uniqColumns 参数不能是空")
	}
	val ks = kvs.joinToString(", ") { it.first }
	val vs = kvs.joinToString(", ") { valPos(it.second) }
	val buf = StringBuilder(512)
	buf.append("INSERT INTO $table ($ks ) VALUES ( $vs ) ")

	val updateCols = kvs.map { it.first }.filter { it !in uniqColumns }
	if (this.isMySQL) {
		buf.append(" ON DUPLICATE KEY UPDATE ")
		val us = updateCols.joinToString(", ") { "$it = VALUES($it) " }
		buf.append(us)
	} else if (this.isPostgres) {
		val kcs = uniqColumns.joinToString(",")
		val uv = updateCols.joinToString(",") { "$it = excluded.$it" }
		buf.append(" ON CONFLICT ($kcs) DO UPDATE SET $uv")
	}
	return this.update(buf.toString(), kvs.map { it.second }) > 0
}

//现有记录和要插入的记录完全一样, 也会返回false, 表示没有更新
fun Connection.insertOrUpdate(model: Model): Boolean {
	val pks = model::class.modelPrimaryKeys
	assert(pks.isNotEmpty())
	val cs = model.modelPropertiesExists
	return this.insertOrUpdate(model::class, cs.map { it to it.getValue(model) }, pks)
}

