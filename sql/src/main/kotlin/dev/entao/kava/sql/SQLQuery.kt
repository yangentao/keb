@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")

package dev.entao.kava.sql

import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.plusAssign
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by yangentao on 2016/12/14.
 */

class SQLQuery {

	//from允许多次调用 from("a").from("b").where....
	private var distinct = false
	private val selectArr = arrayListOf<String>()
	private val fromArr = arrayListOf<String>()
	private var whereClause: String = ""
	private var limitClause: String = ""
	private var joinClause = ""
	private var onClause = ""
	private var orderClause = ""
	private var groupByClause: String = ""
	private var havingClause: String = ""

	val args: ArrayList<Any> = ArrayList()

	fun groupBy(s: String): SQLQuery {
		groupByClause = "GROUP BY $s"
		return this
	}

	fun groupBy(p: Prop): SQLQuery {
		return this.groupBy(p.sqlName)
	}

	fun having(s: String): SQLQuery {
		havingClause = "HAVING $s"
		return this
	}

	fun having(w: Where): SQLQuery {
		this.having(w.value)
		this.args.addAll(w.args)
		return this
	}

	val DISTINCT: SQLQuery
		get() {
			this.distinct = true
			return this
		}

	fun selectAll(): SQLQuery {
		selectArr.add("*")
		return this
	}

	fun select(vararg cols: Prop): SQLQuery {
		cols.mapTo(selectArr) { it.sqlFullName }
		return this
	}

	fun select(vararg cols: String): SQLQuery {
		selectArr.addAll(cols)
		return this
	}

	fun from(vararg clses: TabClass): SQLQuery {
		val ts = clses.map { it.sqlName }
		for (a in ts) {
			if (a !in fromArr) {
				fromArr.add(a)
			}
		}
		return this
	}

	fun from(vararg tables: String): SQLQuery {
		fromArr.addAll(tables)
		return this
	}

	fun join(vararg tables: String): SQLQuery {
		return this.join(tables.toList())
	}

	fun join(vararg modelClasses: TabClass): SQLQuery {
		return join(modelClasses.map { it.sqlName })
	}

	fun join(tables: List<String>, joinType: String = "LEFT"): SQLQuery {
		joinClause = "$joinType JOIN  ${tables.joinToString(", ")}  "
		return this
	}

	fun on(s: String): SQLQuery {
		onClause = " ON $s  "
		return this
	}

	fun on(block: OnBuilder.() -> String): SQLQuery {
		val b = OnBuilder()
		val s = b.block()
		return on(s)
	}

	fun where(block: () -> Where): SQLQuery {
		val w = block.invoke()
		return where(w)
	}

	fun where(w: Where?): SQLQuery {
		if (w != null && w.value.isNotEmpty()) {
			whereClause = "WHERE ${w.value}"
			args.addAll(w.args)
		}
		return this
	}

	fun where(w: String, vararg params: Any): SQLQuery {
		whereClause = "WHERE $w"
		args.addAll(params)
		return this
	}

	fun asc(col: String): SQLQuery {
		if (orderClause.isEmpty()) {
			orderClause = "ORDER BY $col ASC"
		} else {
			orderClause += ", $col ASC"
		}
		return this
	}

	fun desc(col: String): SQLQuery {
		if (orderClause.isEmpty()) {
			orderClause = "ORDER BY $col DESC"
		} else {
			orderClause += ", $col DESC"
		}
		return this
	}

	fun asc(p: Prop): SQLQuery {
		return asc(p.sqlFullName)
	}

	fun desc(p: Prop): SQLQuery {
		return desc(p.sqlFullName)
	}

	fun limit(size: Int): SQLQuery {
		return this.limit(size, 0)
	}

	fun limit(size: Int, offset: Int): SQLQuery {
		if (size > 0 && offset >= 0) {
			limitClause = "LIMIT $size OFFSET $offset "
		}
		return this
	}

	//SELECT owner, COUNT(*) FROM pet GROUP BY owner
	fun toSQL(): String {
		val sb = StringBuilder(256)
		sb += "SELECT "
		if (distinct) {
			sb += "DISTINCT "
		}

		sb += if (selectArr.isEmpty()) {
			"*"
		} else {
			selectArr.joinToString(", ")
		}
		sb.append(" FROM ").append(fromArr.joinToString(","))
		sb += " "
		if (joinClause.isEmpty()) {
			onClause = ""
		}
		if (groupByClause.isEmpty()) {
			havingClause = ""
		}
		val ls = listOf(joinClause, onClause, whereClause, groupByClause, havingClause, orderClause, limitClause)
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