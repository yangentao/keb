@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.log.logd
import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.sql.ext.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * Created by yangentao on 2016/12/14.
 */

fun useMySQL(block: Connection.() -> Unit) {
	SqlConfig.logEnable = true
	val url = "jdbc:mysql://localhost:3306/test?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true"
	val con = OpenUrl(url, "test", "test") ?: return
	con.use(block)
}

fun userPostgres(block: Connection.() -> Unit) {
	Class.forName("org.postgresql.Driver");
	val con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/yangentao", "yangentao", "")
	con.use(block)
}

fun testQuery() {
	useMySQL {
		val ls = SQL(this).selectAll().from(Test::class).where(Test::id LE 11).query().allRows().map {
			val t = Test()
			t.model.putAll(it)
			t
		}

		for (t in ls) {
			logd(t.id, t.name)
		}
	}
}

fun testInsert() {
	useMySQL {
		this.createTable("test", listOf("id int primary key AUTO_INCREMENT", "name varchar(256)"))
		for (i in 1..20) {
			val m = Test()
			m.name = "Name $i "
			this.insert(m)
		}
	}
}

fun dumpResultSets(r: ResultSet) {
	val meta = r.metaData
	val colCount = meta.columnCount
	r.closeAfter {
		while (r.next()) {
			for (i in 1..meta.columnCount) {
				logd("CAT:", meta.getCatalogName(i), "SCM:", meta.getSchemaName(i), "TAB:", meta.getTableName(i),
						"COL", meta.getColumnName(i), "COLLB:", meta.getColumnLabel(i),
						"CLS:", meta.getColumnClassName(i), "TYP:", meta.getColumnTypeName(i), "VAL:", r.getObject(i))
			}
		}
	}
}

fun main() {
//	useMySQL {
//		val md = this.metaData
//		logd(md.databaseProductName)
//		logd(md.driverName)
//		val r = SQL(this).select("test.id", "test.name as testName").from(Test::class).query()
//		dumpResultSets(r)
//	}

	userPostgres {
		logd(this.metaData.databaseProductName)
		val r  =this.metaData.tableTypes
		r.dump()
	}
}

class SelOpt {
	var distinct = false
}

val Prop.s: String get() = this.sqlFullName
val KClass<*>.s: String get() = this.sqlName

infix fun String.AS(other: String): String {
	return "$this AS $other"
}


class SQL(val conn: Connection? = null) {
	private val buf = StringBuilder(512)
	val args: ArrayList<Any?> = ArrayList()

	constructor(cls: KClass<*>) : this(cls.namedConn)

	val sql: String get() = buf.toString()

	fun clearBuf() {
		buf.setLength(0)
		args.clear()
	}

	fun update(cls: KClass<*>, map: Map<Prop, Any?>): SQL {
		return this.update(cls.s, map.mapKeys { it.key.s })
	}

	fun update(cls: KClass<*>, list: List<Pair<Prop, Any?>>): SQL {
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
		return this.insert(modelCls.sqlName, kvs.map { it.first.sqlFullName to it.second })
	}

	fun insert(table: String, kvs: List<Pair<String, Any?>>): SQL {
		val keyList = kvs.map { it.first }
		buf.append("INSERT INTO $table (")
		val ks = keyList.joinToString(", ")
		val vs = kvs.joinToString(", ") { "?" }
		buf.append("$ks ) VALUES ( $vs ) ")
		args.addAll(kvs.map { it.second })
		return this
	}

	fun replace(modelCls: KClass<*>, kvs: List<Pair<Prop1, Any?>>): SQL {
		return this.replace(modelCls.sqlName, kvs.map { it.first.sqlFullName to it.second })
	}

	fun replace(table: String, kvs: List<Pair<String, Any?>>): SQL {
		val keyList = kvs.map { it.first }
		buf.append("REPLACE INTO $table (")
		val ks = keyList.joinToString(", ")
		val vs = kvs.joinToString(", ") { "?" }
		buf.append("$ks ) VALUES ( $vs ) ")
		args.addAll(kvs.map { it.second })
		return this
	}

	fun insertOrUpdate(modelCls: KClass<*>, pkColumns: List<Prop1>, kvs: List<Pair<Prop1, Any?>>): SQL {
		return this.insertOrUpdate(modelCls.sqlName, pkColumns.map { it.sqlFullName }, kvs.map { it.first.sqlFullName to it.second })
	}

	fun insertOrUpdate(table: String, pkColumns: List<String>, kvs: List<Pair<String, Any?>>): SQL {
		if (pkColumns.isEmpty()) {
			throw IllegalArgumentException("insertOrUpdate $table  uniqeColumns参数不能是空")
		}
		val keyList = kvs.map { it.first }
		buf.append("INSERT INTO $table (")
		val ks = keyList.joinToString(", ")
		val vs = kvs.joinToString(", ") { "?" }
		buf.append("$ks ) VALUES ( $vs ) ")
		buf.append(" ON DUPLICATE KEY UPDATE ")
		val pkList = pkColumns.map { it }
		val us = keyList.filter { it !in pkList }.joinToString(", ") { "$it = VALUES($it) " }
		buf.append(us)
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

	fun from(vararg clses: KClass<*>): SQL {
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

	fun join(vararg modelClasses: KClass<*>): SQL {
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
		if (w != null) {
			buf.append(" WHERE ")
			buf.append(w.value)
			args.addAll(w.args)
		}
		return this
	}

	fun where(w: String, vararg params: Any): SQL {
		buf.append(" WHERE ")
		buf.append(w)
		args.addAll(params)
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

	fun query(): ResultSet {
		return conn!!.query(sql, args)
	}

	fun update(): Int {
		return conn!!.update(sql, args)
	}

	fun insert(): Int {
		return conn!!.update(sql, args)
	}

	fun insertGenKey(): Long {
		val st = conn!!.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
		st.setParams(args)
		val n = st.executeUpdate()
		val r: Long = if (n <= 0) {
			0L
		} else {
			st.generatedKeys.longValue ?: 0L
		}
		st.close()
		return r
	}

}

class OnBuilder {

	infix fun Prop1.EQ(s: Prop1): String {
		return "${this.sqlFullName} = ${s.sqlFullName}"
	}

	infix fun String.EQ(s: String): String {
		return "$this = $s"
	}

	infix fun String.AND(s: String): String {
		return "$this AND $s"
	}
}