package dev.entao.kava.sql

import dev.entao.kava.base.Prop
import dev.entao.kava.base.plusAssign

//Single Table Query
class TableQuery(val tableName: String) {

	private var distinct = false
	private val selectArr = arrayListOf<String>()
	private var whereClause: String = ""
	private var limitClause: String = ""
	private var orderClause = ""
	private var groupByClause: String = ""
	private var havingClause: String = ""

	val args: ArrayList<Any> = ArrayList()

	fun groupBy(s: String): TableQuery {
		groupByClause = "GROUP BY $s"
		return this
	}

	fun groupBy(p: Prop): TableQuery {
		return this.groupBy(p.sqlName)
	}

	fun having(s: String): TableQuery {
		havingClause = "HAVING $s"
		return this
	}

	fun having(w: Where): TableQuery {
		this.having(w.value)
		this.args.addAll(w.args)
		return this
	}

	val DISTINCT: TableQuery
		get() {
			this.distinct = true
			return this
		}

	fun selectAll(): TableQuery {
		selectArr.add("*")
		return this
	}

	fun select(vararg cols: Prop): TableQuery {
		cols.mapTo(selectArr) { it.sqlFullName }
		return this
	}

	fun select(vararg cols: String): TableQuery {
		selectArr.addAll(cols)
		return this
	}


	fun where(block: () -> Where): TableQuery {
		val w = block.invoke()
		return where(w)
	}

	fun where(w: Where?): TableQuery {
		if (w != null && w.value.isNotEmpty()) {
			whereClause = "WHERE ${w.value}"
			args.addAll(w.args)
		}
		return this
	}

	fun where(w: String, vararg params: Any): TableQuery {
		whereClause = "WHERE $w"
		args.addAll(params)
		return this
	}

	fun asc(col: String): TableQuery {
		if (orderClause.isEmpty()) {
			orderClause = "ORDER BY $col ASC"
		} else {
			orderClause += ", $col ASC"
		}
		return this
	}

	fun desc(col: String): TableQuery {
		if (orderClause.isEmpty()) {
			orderClause = "ORDER BY $col DESC"
		} else {
			orderClause += ", $col DESC"
		}
		return this
	}

	fun asc(p: Prop): TableQuery {
		return asc(p.sqlFullName)
	}

	fun desc(p: Prop): TableQuery {
		return desc(p.sqlFullName)
	}

	fun limit(size: Int): TableQuery {
		return this.limit(size, 0)
	}

	fun limit(size: Int, offset: Int): TableQuery {
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
		sb.append(" FROM ").append(tableName)
		sb += " "
		if (groupByClause.isEmpty()) {
			havingClause = ""
		}
		val ls = listOf(whereClause, groupByClause, havingClause, orderClause, limitClause)
		sb += ls.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
		return sb.toString()
	}

}