@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName", "FunctionName")

package dev.entao.kava.sql

import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.plusAssign
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KMutableProperty

/**
 * Created by yangentao on 2016/12/14.
 */

@DslMarker
annotation class SQLMarker

@SQLMarker
class OrderByClause(val _colList: ArrayList<String> = ArrayList()) {

	val isEmpty: Boolean get() = _colList.isEmpty()

	fun asc(col: String) {
		_colList += "$col ASC"
	}

	fun desc(col: String) {
		_colList += "$col DESC"
	}

	fun asc(p: Prop) {
		asc(p.sqlFullName)
	}

	fun desc(p: Prop) {
		desc(p.sqlFullName)
	}

	override fun toString(): String {
		if (isEmpty) {
			return ""
		}
		return "ORDER BY " + _colList.joinToString(",")
	}
}

@SQLMarker
class SelectClause {
	var _distinct: String = ""
	val columns = ArrayList<String>()


	operator fun String.unaryPlus() {
		columns += this
	}

	operator fun Prop.unaryPlus() {
		columns += this.sqlFullName
	}

	fun cols(vararg cs: String) {
		columns += cs
	}

	fun cols(vararg cs: Prop) {
		columns += cs.map { it.sqlFullName }
	}

	fun cols(cols: List<Prop>) {
		columns.addAll(cols.map { it.sqlFullName })
	}

	val distinct: SelectClause
		get() {
			_distinct = "DISTINCT"
			return this
		}

	fun distinctOn(prop: Prop) = distinctOn(prop.sqlFullName)
	fun distinctOn(col: String) {
		_distinct = "DISTINCT ON($col)"
	}


	fun call(funName: String, arg: String) {
		columns += "$funName($arg)"
	}

	fun count(col: String) = call("COUNT", col)
	fun count(p: Prop) = call("COUNT", p.sqlFullName)

	fun countDistinct(col: String) = call("COUNT", "DISTINCT $col")
	fun countDistinct(p: Prop) = call("COUNT", "DISTINCT ${p.sqlFullName}")
	fun sum(col: String) = call("SUM", col)
	fun sum(p: Prop) = call("SUM", p.sqlFullName)
	fun max(col: String) = call("MAX", col)
	fun max(p: Prop) = call("MAX", p.sqlFullName)
	fun min(col: String) = call("MIN", col)
	fun min(p: Prop) = call("MIN", p.sqlFullName)
	fun avg(col: String) = call("AVG", col)
	fun avg(p: Prop) = call("AVG", p.sqlFullName)


	fun lead(p: Prop, offset: Int = 1, default: String? = null) {
		lead(p.sqlFullName, offset, default)
	}

	fun lead(col: String, offset: Int = 1, default: String? = null) {
		columns += when {
			default != null -> {
				"LEAD($col, $offset, $default)"
			}
			offset > 1 -> {
				"LEAD($col, $offset)"
			}
			else -> {
				"LEAD($col)"
			}
		}
	}

	fun lag(p: Prop, offset: Int = 1, default: String? = null) {
		lag(p.sqlFullName, offset, default)
	}

	fun lag(col: String, offset: Int = 1, default: String? = null) {
		columns += when {
			default != null -> {
				"LAG($col, $offset, $default)"
			}
			offset > 1 -> {
				"LAG($col, $offset)"
			}
			else -> {
				"LAG($col)"
			}
		}
	}


	fun row_number() {
		columns += "ROW_NUMBER()"
	}

	fun rank() {
		columns += "RANK()"
	}

	fun dense_rank() {
		columns += "DENSE_RANK()"
	}

	fun percent_rank() {
		columns += "PERCENT_RANK()"
	}


	fun first_value(p: Prop, ignoreNulls: Boolean = false) {
		first_value(p.sqlFullName, ignoreNulls)
	}

	fun first_value(col: String, ignoreNulls: Boolean = false) {
		columns += if (ignoreNulls) {
			"FIRST_VALUE($col ignore nulls)"
		} else {
			"FIRST_VALUE($col)"
		}
	}

	fun last_value(p: Prop, ignoreNulls: Boolean = false) {
		last_value(p.sqlFullName, ignoreNulls)
	}

	fun last_value(col: String, ignoreNulls: Boolean = false) {
		columns += if (ignoreNulls) {
			"LAST_VALUE($col ignore nulls)"
		} else {
			"LAST_VALUE($col)"
		}
	}

	fun over(block: OverClause.() -> Unit) {
		val ov = OverClause()
		ov.block()
		columns[columns.lastIndex] = columns.last() + ov.toString()
	}

	override fun toString(): String {
		val buf = StringBuilder(columns.size * 12 + 16)
		buf += "SELECT "
		if (_distinct.isNotEmpty()) {
			buf += _distinct
			buf += " "
		}
		val cs = columns.joinToString(", ")
		if (cs.isEmpty()) {
			buf += "* "
		} else {
			buf += cs
		}
		return buf.toString()
	}
}


@SQLMarker
class OverClause {
	var _alias: String = ""
	var _partitionby: String = ""
	val _orderClause = OrderByClause()
	var _range: String = ""

	fun alias(alias: String) {
		this._alias = alias
	}

	fun partitionBy(p: Prop) {
		partitionBy(p.sqlFullName)
	}

	fun partitionBy(col: String) {
		_partitionby = "PARTITION BY $col"
	}

	fun asc(col: String) {
		_orderClause.asc(col)
	}

	fun desc(col: String) {
		_orderClause.desc(col)
	}

	fun asc(p: Prop) {
		asc(p.sqlFullName)
	}

	fun desc(p: Prop) {
		desc(p.sqlFullName)
	}

	fun orderBy(block: OrderByClause.() -> Unit) {
		_orderClause.block()
	}

	fun rangeBetween(preceding: Int, following: Int) {
		val a = if (preceding >= 0) {
			preceding.toString()
		} else {
			"unbounded"
		}
		val b = if (following >= 0) {
			following.toString()
		} else {
			"unbounded"
		}
		_range = "range between $a preceding and $b following"
	}

	fun rowsBetween(preceding: Int, following: Int) {
		val a = if (preceding >= 0) {
			preceding.toString()
		} else {
			"unbounded"
		}
		val b = if (following >= 0) {
			following.toString()
		} else {
			"unbounded"
		}
		_range = "rows between $a preceding and $b following"
	}

	override fun toString(): String {
		return " OVER($_partitionby $_orderClause  $_range) $_alias"
	}
}

@SQLMarker
abstract class BaseQuery {
	var _seleectClause = SelectClause()
	var _whereClause: String = ""
	var _limitClause: String = ""
	val _orderByClause = OrderByClause()
	var _groupByClause: String = ""
	var _havingClause: String = ""


	val args: ArrayList<Any?> = ArrayList()

	abstract fun toSQL(): String
}

fun <T : BaseQuery> T.select(cols: List<Prop>): T {
	_seleectClause.columns.addAll(cols.map { it.sqlFullName })
	return this
}


fun <T : BaseQuery> T.select(block: SelectClause.() -> Unit): T {
	_seleectClause.block()
	return this
}

fun <T : BaseQuery> T.distinct(): T {
	_seleectClause.distinct
	return this
}

fun <T : BaseQuery> T.distinctOn(col: String): T {
	_seleectClause.distinctOn(col)
	return this
}

fun <T : BaseQuery> T.distinctOn(p: Prop): T {
	_seleectClause.distinctOn(p)
	return this
}

fun <T : BaseQuery> T.selectAll(): T {
	_seleectClause.cols("*")
	return this
}

fun <T : BaseQuery> T.select(vararg cols: Prop): T {
	_seleectClause.cols(*cols)
	return this
}

fun <T : BaseQuery> T.select(vararg cols: String): T {
	_seleectClause.cols(*cols)
	return this
}

fun <T : BaseQuery> T.groupBy(s: String): T {
	_groupByClause = "GROUP BY $s"
	return this
}

fun <T : BaseQuery> T.groupBy(p: Prop): T {
	return this.groupBy(p.sqlName)
}

fun <T : BaseQuery> T.having(s: String): T {
	_havingClause = "HAVING $s"
	return this
}

fun <T : BaseQuery> T.having(w: Where): T {
	this.having(w.value)
	this.args.addAll(w.args)
	return this
}


fun <T : BaseQuery> T.where(block: () -> Where): T {
	val w = block.invoke()
	return where(w)
}

fun <T : BaseQuery> T.where(w: Where?): T {
	if (w != null && w.value.isNotEmpty()) {
		_whereClause = "WHERE ${w.value}"
		args.addAll(w.args)
	}
	return this
}

fun <T : BaseQuery> T.where(w: String, vararg params: Any): T {
	_whereClause = "WHERE $w"
	args.addAll(params)
	return this
}

fun <T : BaseQuery> T.asc(col: String): T {
	_orderByClause.asc(col)
	return this
}

fun <T : BaseQuery> T.desc(col: String): T {
	_orderByClause.desc(col)
	return this
}

fun <T : BaseQuery> T.asc(p: Prop): T {
	return asc(p.sqlFullName)
}

fun <T : BaseQuery> T.desc(p: Prop): T {
	return desc(p.sqlFullName)
}

fun <T : BaseQuery> T.limit(size: Int): T {
	return this.limit(size, 0)
}

fun <T : BaseQuery> T.limit(size: Int, offset: Int): T {
	if (size > 0 && offset >= 0) {
		_limitClause = "LIMIT $size OFFSET $offset "
	}
	return this
}

class TableQuery(val tableName: String) : BaseQuery() {

	constructor(cls: TabClass) : this(cls.sqlName) {

	}

	//SELECT owner, COUNT(*) FROM pet GROUP BY owner
	override fun toSQL(): String {
		val sb = StringBuilder(256)
		sb += _seleectClause.toString()
		sb.append(" FROM ").append(tableName)
		sb += " "
		if (_groupByClause.isEmpty()) {
			_havingClause = ""
		}
		val ls = listOf(_whereClause, _groupByClause, _havingClause, _orderByClause.toString(), _limitClause)
		sb += ls.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
		return sb.toString()
	}
}

@SQLMarker
class SQLQuery : BaseQuery() {

	//from允许多次调用 from("a").from("b").where....
	val _fromClause = arrayListOf<String>()
	var _joinType = ""
	var _joinClause = ""
	var _onClause = ""

	fun from(vararg clses: TabClass): SQLQuery {
		val ts = clses.map { it.sqlName }
		for (a in ts) {
			if (a !in _fromClause) {
				_fromClause.add(a)
			}
		}
		return this
	}

	fun from(vararg tables: String): SQLQuery {
		for (a in tables) {
			if (a !in _fromClause) {
				_fromClause.add(a)
			}
		}
		return this
	}

	val INNER: SQLQuery
		get() {
			_joinType = "INNER"
			return this
		}
	val LEFT: SQLQuery
		get() {
			_joinType = "LEFT"
			return this
		}
	val RIGHT: SQLQuery
		get() {
			_joinType = "RIGHT"
			return this
		}
	val FULL: SQLQuery
		get() {
			_joinType = "FULL"
			return this
		}
	val LEFT_OUTER: SQLQuery
		get() {
			_joinType = "LEFT OUTER"
			return this
		}
	val RIGHT_OUTER: SQLQuery
		get() {
			_joinType = "RIGHT OUTER"
			return this
		}
	val FULL_OUTER: SQLQuery
		get() {
			_joinType = "FULL OUTER"
			return this
		}

	fun join(vararg tables: String): SQLQuery {
		return this.join(tables.toList())
	}

	fun join(vararg modelClasses: TabClass): SQLQuery {
		return join(modelClasses.map { it.sqlName })
	}

	fun join(tables: List<String>): SQLQuery {
		_joinClause = "JOIN  ${tables.joinToString(", ")}  "
		return this
	}

	fun on(s: String): SQLQuery {
		_onClause = " ON $s  "
		return this
	}

	fun on(block: OnBuilder.() -> String): SQLQuery {
		val b = OnBuilder()
		val s = b.block()
		return on(s)
	}

	fun joinOn(table: String, block: OnBuilder.() -> String): SQLQuery {
		join(table)
		on(block)
		return this
	}

	fun joinOn(cls: TabClass, block: OnBuilder.() -> String): SQLQuery {
		join(cls.sqlName)
		on(block)
		return this
	}


	//SELECT owner, COUNT(*) FROM pet GROUP BY owner
	override fun toSQL(): String {
		val sb = StringBuilder(256)
		sb += _seleectClause.toString()
		sb.append(" FROM ").append(_fromClause.joinToString(","))
		sb += " "
		if (_joinClause.isEmpty()) {
			_onClause = ""
			_joinType = ""
		}
		if (_groupByClause.isEmpty()) {
			_havingClause = ""
		}
		val ls = listOf(_joinType, _joinClause, _onClause, _whereClause, _groupByClause, _havingClause, _orderByClause.toString(), _limitClause)
		sb += ls.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
		return sb.toString()
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

fun UNION(vararg qs: BaseQuery): SQLArgs {
	val sql = qs.joinToString(" UNION ") { it.toSQL() }
	val ls = ArrayList<Any?>()
	qs.forEach {
		ls.addAll(it.args)
	}
	return SQLArgs(sql, ls)
}

fun UNION_ALL(vararg qs: BaseQuery): SQLArgs {
	val sql = qs.joinToString(" UNION ALL ") { it.toSQL() }
	val ls = ArrayList<Any?>()
	qs.forEach {
		ls.addAll(it.args)
	}
	return SQLArgs(sql, ls)
}

fun Connection.query(q: SQLQuery): ResultSet {
	return this.query(q.toSQL(), q.args)
}

fun Connection.query(block: SQLQuery.() -> Unit): ResultSet {
	val q = SQLQuery()
	q.block()
	return this.query(q.toSQL(), q.args)
}

fun Connection.dump(block: SQLQuery.() -> Unit) {
	val q = SQLQuery()
	q.block()
	val sql = q.toSQL()
	this.query(sql, emptyList()).dump()
}