@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")

package dev.entao.kava.sql

import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.plusAssign
import java.sql.Connection
import java.sql.ResultSet

/**
 * Created by yangentao on 2016/12/14.
 */


//Single Table Query
open class BaseQuery {
	var _distinctClause: String = ""
	val _selectClause = arrayListOf<String>()
	var _whereClause: String = ""
	var _limitClause: String = ""
	var _orderClause = ""
	var _groupByClause: String = ""
	var _havingClause: String = ""

	val args: ArrayList<Any> = ArrayList()
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

fun <T : BaseQuery> T.distinct(): T {
	this._distinctClause = "DISTINCT"
	return this
}

fun <T : BaseQuery> T.distinctOn(col: String): T {
	this._distinctClause = "DISTINCT ON($col)"
	return this
}

fun <T : BaseQuery> T.distinctOn(p: Prop): T {
	this._distinctClause = "DISTINCT ON(${p.sqlFullName})"
	return this
}

fun <T : BaseQuery> T.selectAll(): T {
	_selectClause.add("*")
	return this
}

fun <T : BaseQuery> T.select(vararg cols: Prop): T {
	cols.mapTo(_selectClause) { it.sqlFullName }
	return this
}

fun <T : BaseQuery> T.select(vararg cols: String): T {
	_selectClause.addAll(cols)
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
	if (_orderClause.isEmpty()) {
		_orderClause = "ORDER BY $col ASC"
	} else {
		_orderClause += ", $col ASC"
	}
	return this
}

fun <T : BaseQuery> T.desc(col: String): T {
	if (_orderClause.isEmpty()) {
		_orderClause = "ORDER BY $col DESC"
	} else {
		_orderClause += ", $col DESC"
	}
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
	//SELECT owner, COUNT(*) FROM pet GROUP BY owner
	fun toSQL(): String {
		val sb = StringBuilder(256)
		sb += "SELECT "
		if (_distinctClause.isNotEmpty()) {
			sb += _distinctClause
			sb += " "
		}

		sb += if (_selectClause.isEmpty()) {
			"*"
		} else {
			_selectClause.joinToString(", ")
		}
		sb.append(" FROM ").append(tableName)
		sb += " "
		if (_groupByClause.isEmpty()) {
			_havingClause = ""
		}
		val ls = listOf(_whereClause, _groupByClause, _havingClause, _orderClause, _limitClause)
		sb += ls.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
		return sb.toString()
	}
}


class SQLQuery : BaseQuery() {

	//from允许多次调用 from("a").from("b").where....
	val _fromClause = arrayListOf<String>()
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

	fun join(vararg tables: String): SQLQuery {
		return this.join(tables.toList())
	}

	fun join(vararg modelClasses: TabClass): SQLQuery {
		return join(modelClasses.map { it.sqlName })
	}

	fun join(tables: List<String>, joinType: String = "LEFT"): SQLQuery {
		_joinClause = "$joinType JOIN  ${tables.joinToString(", ")}  "
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


	//SELECT owner, COUNT(*) FROM pet GROUP BY owner
	fun toSQL(): String {
		val sb = StringBuilder(256)
		sb += "SELECT "
		if (_distinctClause.isNotEmpty()) {
			sb += _distinctClause
			sb += " "
		}

		sb += if (_selectClause.isEmpty()) {
			"*"
		} else {
			_selectClause.joinToString(", ")
		}
		sb.append(" FROM ").append(_fromClause.joinToString(","))
		sb += " "
		if (_joinClause.isEmpty()) {
			_onClause = ""
		}
		if (_groupByClause.isEmpty()) {
			_havingClause = ""
		}
		val ls = listOf(_joinClause, _onClause, _whereClause, _groupByClause, _havingClause, _orderClause, _limitClause)
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