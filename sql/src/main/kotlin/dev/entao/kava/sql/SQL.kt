@file:Suppress("unused", "FunctionName", "MemberVisibilityCanBePrivate")

package dev.entao.kava.sql

import dev.entao.kava.base.Name
import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.ownerClass
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Created by yangentao on 2016/12/14.
 */

val KClass<*>.sqlName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.simpleName!!.toLowerCase()
	}

val Prop.sqlName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.name.toLowerCase()
	}
val Prop.sqlFullName: String
	get() {
		return "${this.ownerClass!!.sqlName}.${this.sqlName}"
	}

typealias TabClass = KClass<*>

val Prop.s: String get() = this.sqlName

val TabClass.s: String get() = this.sqlName

class SelOpt {
	var distinct = false
}


infix fun String.AS(other: String): String {
	return "$this AS $other"
}


class SQL {
	private val buf = StringBuilder(512)
	val args: ArrayList<Any?> = ArrayList()
	val sql: String get() = buf.toString()

	fun update(cls: TabClass, map: Map<Prop, Any?>): SQL {
		return this.update(cls.s, map.mapKeys { it.key.s })
	}

	fun update(cls: TabClass, list: List<Pair<Prop, Any?>>): SQL {
		return this.update(cls.s, list.map { it.first.s to it.second })
	}

	fun update(table: String, list: List<Pair<String, Any?>>): SQL {
		buf.append("UPDATE $table SET ")
		val s = list.joinToString(", ") { it.first + " = ?" }
		buf.append(s).append(" ")
		args.addAll(list.map { it.second })
		return this
	}

	fun update(table: String, map: Map<String, Any?>): SQL {
		buf.append("UPDATE $table SET ")
		val s = map.map { it.key + " = ?" }.joinToString(", ")
		buf.append(s).append(" ")
		args.addAll(map.map { it.value })
		return this
	}

	fun deleteFrom(modelCls: KClass<*>): SQL {
		return this.deleteFrom(modelCls.sqlName)
	}

	fun deleteFrom(table: String): SQL {
		buf.append("DELETE FROM $table ")
		return this
	}

	fun insert(modelCls: KClass<*>, kvs: List<Pair<Prop1, Any?>>): SQL {
		return this.insert(modelCls.sqlName, kvs.map { it.first.sqlName to it.second })
	}

	fun insert(table: String, kvs: List<Pair<String, Any?>>): SQL {
		val ks = kvs.joinToString(", ") { it.first }
		val vs = kvs.joinToString(", ") { "?" }
		buf.append("INSERT INTO $table ($ks) VALUES ($vs) ")
		args.addAll(kvs.map { it.second })
		return this
	}

	fun insertOrUpdateMySqL(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>, updateColumns: List<Prop>): SQL {
		return this.insertOrUpdateMySqL(modelCls.sqlName, kvs.map { it.first.sqlName to it.second }, updateColumns.map { it.sqlName })
	}

	fun insertOrUpdateMySqL(table: String, kvs: List<Pair<String, Any?>>, updateColumns: List<String>): SQL {
		if (updateColumns.isEmpty()) {
			throw IllegalArgumentException("insertOrUpdate $table  updateColumns参数不能是空")
		}
		val ks = kvs.joinToString(", ") { it.first }
		val vs = kvs.joinToString(", ") { "?" }
		buf.append("INSERT INTO $table ($ks ) VALUES ( $vs ) ")
		buf.append(" ON DUPLICATE KEY UPDATE ")
		val us = kvs.map { it.first }.filter { it in updateColumns }.joinToString(", ") { "$it = VALUES($it) " }
		buf.append(us)
		args.addAll(kvs.map { it.second })
		return this
	}

	fun insertOrUpdatePG(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>, updateColumns: List<Prop>): SQL {
		return this.insertOrUpdatePG(modelCls.sqlName, kvs.map { it.first.sqlName to it.second }, updateColumns.map { it.sqlName })
	}

	fun insertOrUpdatePG(table: String, kvs: List<Pair<String, Any?>>, updateColumns: List<String>): SQL {
		if (updateColumns.isEmpty()) {
			throw IllegalArgumentException("insertOrUpdate $table  updateColumns参数不能是空")
		}
		val ks = kvs.joinToString(", ") { it.first }
		val vs = kvs.joinToString(", ") { "?" }
		buf.append("INSERT INTO $table ($ks ) VALUES ( $vs ) ")
		val uc = updateColumns.joinToString(",")
		val uv = updateColumns.joinToString(",") { "excluded.$it" }
		buf.append(" ON CONFLICT DO UPDATE SET ($uc)=($uv)")
		args.addAll(kvs.map { it.second })
		return this
	}

	fun selectAll(): SQL {
		return select(emptyList())
	}

	fun select(vararg cols: String): SQL {
		return this.select(cols.toList())
	}

	fun select(cols: List<String>): SQL {
		return this.select(cols) {}
	}

	fun select(cols: List<String>, block: SelOpt.() -> Unit): SQL {
		val opt = SelOpt()
		opt.block()
		buf.append("SELECT ")
		if (opt.distinct) {
			buf.append("DISTINCT ")
		}
		if (cols.isEmpty()) {
			buf.append("*")
		} else {
			buf.append(cols.joinToString(", "))
		}
		return this
	}

	fun selectCount(col: Prop): SQL {
		return this.selectCount(col.sqlFullName) {}
	}

	fun selectCount(col: String, block: SelOpt.() -> Unit): SQL {
		val opt = SelOpt()
		opt.block()
		if (opt.distinct) {
			buf.append("SELECT COUNT( DISTINCT $col)")
		} else {
			buf.append("SELECT COUNT($col)")
		}
		return this
	}

	fun from(vararg clses: TabClass): SQL {
		return from(clses.map { it.sqlName })
	}

	fun from(vararg tables: String): SQL {
		return from(tables.toList())
	}

	fun from(tables: List<String>): SQL {
		buf.append(" FROM ")
		buf.append(tables.joinToString(", "))
		return this
	}

	fun join(vararg modelClasses: TabClass): SQL {
		return join(modelClasses.map { it.sqlName })
	}

	fun join(vararg tables: String, joinType: String = "LEFT"): SQL {
		return this.join(tables.toList(), joinType)
	}

	fun join(tables: List<String>, joinType: String = "LEFT"): SQL {
		buf.append(" ")
		buf.append(joinType)
		buf.append(" JOIN (")
		buf.append(tables.joinToString(", "))
		buf.append(" ) ")
		return this
	}

	fun on(s: String): SQL {
		buf.append(" ON ($s) ")
		return this
	}

	fun on(block: OnBuilder.() -> String): SQL {
		val b = OnBuilder()
		val s = b.block()
		return on(s)
	}

	fun where(w: Where?): SQL {
		if (w != null && w.value.isNotEmpty()) {
			buf.append(" WHERE ")
			buf.append(w.value)
			args.addAll(w.args)
		}
		return this
	}

	fun where(w: String, vararg params: Any): SQL {
		if (w.isNotEmpty()) {
			buf.append(" WHERE ")
			buf.append(w)
			args.addAll(params)
		}
		return this
	}

	fun groupBy(s: String): SQL {
		buf.append(" GROUP BY $s")
		return this
	}

	fun groupBy(p: Prop): SQL {
		return this.groupBy(p.sqlFullName)
	}

	fun having(w: Where): SQL {
		buf.append(" HAVING ")
		buf.append(w.value)
		args.addAll(w.args)
		return this
	}

	fun asc(col: String): SQL {
		if (buf.contains("ORDER BY")) {
			buf.append(", $col ASC")
		} else {
			buf.append(" ORDER BY $col ASC")
		}
		return this
	}

	fun desc(col: String): SQL {
		if (buf.contains("ORDER BY")) {
			buf.append(", $col DESC")
		} else {
			buf.append(" ORDER BY $col DESC")
		}
		return this
	}

	fun asc(p: Prop): SQL {
		return asc(p.sqlFullName)
	}

	fun desc(p: Prop): SQL {
		return desc(p.sqlFullName)
	}

	fun limit(size: Int): SQL {
		return this.limit(size, 0)
	}

	fun limit(size: Int, offset: Int): SQL {
		buf.append(" LIMIT $size OFFSET $offset")
		return this
	}
}

class OnBuilder {

	infix fun Prop.EQ(s: Prop1): String {
		return "${this.sqlFullName} = ${s.sqlFullName}"
	}

	infix fun String.EQ(s: String): String {
		return "$this = $s"
	}

	infix fun String.AND(s: String): String {
		return "$this AND $s"
	}
}

fun Connection.query(a: SQL): ResultSet {
	return this.query(a.sql, a.args)
}

fun Connection.querySQL(block: SQL.() -> Unit): ResultSet {
	val a = SQL()
	a.block()
	return this.query(a.sql, a.args)
}

fun Connection.update(a: SQL): Int {
	return this.update(a.sql, a.args)
}

fun Connection.updateSQL(block: SQL.() -> Unit): Int {
	val a = SQL()
	a.block()
	return this.update(a.sql, a.args)
}

fun Connection.insert(a: SQL): Int {
	return this.update(a.sql, a.args)
}

fun Connection.insertSQL(block: SQL.() -> Unit): Int {
	val a = SQL()
	a.block()
	return this.update(a.sql, a.args)
}

fun Connection.insertSQLGenKey(a: SQL): Long {
	return this.insertGenKey(a.sql, a.args)
}

fun Connection.insertSQLGenKey(block: SQL.() -> Unit): Long {
	val a = SQL()
	a.block()
	return this.insertGenKey(a.sql, a.args)
}