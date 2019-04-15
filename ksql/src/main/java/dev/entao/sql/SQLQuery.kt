@file:Suppress("unused")

package dev.entao.sql

import dev.entao.kava.base.Prop
import dev.entao.kava.base.plusAssign
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
		return this.groupBy(p.s)
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

	fun distinct(): SQLQuery {
		this.distinct = true
		return this
	}

	fun selectAll(): SQLQuery {
		selectArr.add("*")
		return this
	}

	fun select(vararg cols: KProperty<*>): SQLQuery {
		cols.mapTo(selectArr) { it.sqlFullName }
		return this
	}

	fun select(vararg cols: String): SQLQuery {
		selectArr.addAll(cols)
		return this
	}

	fun from(vararg clses: KClass<*>): SQLQuery {
		clses.mapTo(fromArr) { it.sqlName }
		return this
	}

	fun from(vararg tables: String): SQLQuery {
		fromArr.addAll(tables.map {
			if (it.startsWith("`")) {
				it
			} else {
				"`$it`"
			}
		})
		return this
	}

	fun join(vararg tables: String): SQLQuery {
		return this.join(tables.toList())
	}

	fun join(vararg modelClasses: KClass<*>): SQLQuery {
		return join(modelClasses.map { it.sqlName })
	}

	fun join(tables: List<String>, joinType: String = "LEFT"): SQLQuery {
		joinClause = "$joinType JOIN ( ${tables.joinToString(", ")} ) "
		return this
	}

	fun on(s: String): SQLQuery {
		onClause = " ON ($s) "
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
		if (w != null) {
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

	fun asc(p: KProperty<*>): SQLQuery {
		return asc(p.sqlFullName)
	}

	fun desc(p: KProperty<*>): SQLQuery {
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
