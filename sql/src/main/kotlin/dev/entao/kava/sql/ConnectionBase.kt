package dev.entao.kava.sql

import dev.entao.kava.json.*
import dev.entao.kava.log.logd
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

val Connection.isMySQL: Boolean get() = "MySQL" == this.metaData.databaseProductName
val Connection.isPostgres: Boolean get() = "PostgreSQL" == this.metaData.databaseProductName

fun PreparedStatement.setParams(params: List<Any?>) {
	for (i in params.indices) {
		val v = params[i]
		val vv: Any? = when (v) {
			is YsonNull -> null
			is YsonObject -> v.toString()
			is YsonArray -> v.toString()
			is YsonString -> v.toString()
			is YsonNum -> v.data
			is YsonBool -> v.data
			is YsonBlob -> v.data
			else -> v

		}
		this.setObject(i + 1, vv)
	}
}

fun Connection.exec(sql: String, args: List<Any> = emptyList()): Boolean {
	if (ConnLook.logEnable) {
		logd(sql)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	return st.use {
		it.execute()
	}
}

fun Connection.query(sql: String, args: List<Any?>): ResultSet {
	if (ConnLook.logEnable) {
		logd(sql)
		logd(args)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	return st.executeQuery()
}

fun Connection.update(sql: String, args: List<Any?> = emptyList()): Int {
	if (ConnLook.logEnable) {
		logd(sql)
		logd(args)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	return st.use {
		it.executeUpdate()
	}
}

fun Connection.insertGenKey(sql: String, args: ArrayList<Any?>): Long {
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


fun Connection.createTable(tableName: String, columns: List<String>): Int {
	val sql = buildString {
		append("CREATE TABLE IF NOT EXISTS $tableName (")
		append(columns.joinToString(", "))
		append(")")
	}
	return this.update(sql)
}

fun Connection.tableExists(tableName: String): Boolean {
	val meta = this.metaData
	val rs = meta.getTables(this.catalog, this.schema, tableName, arrayOf("TABLE"))
	val s = rs.firstRow()?.get("TABLE_NAME")?.toString() ?: ""
	return s.toLowerCase() == tableName.toLowerCase()
}

fun Connection.tableDesc(tableName: String): List<ColumnInfo> {
	val meta = this.metaData
	val rs = meta.getColumns(this.catalog, this.schema, tableName, "%")
	return rs.allRows().map {
		val m = ColumnInfo()
		m.model.putAll(it)
		m
	}
}

fun Connection.dumpIndex(tableName: String) {
	val meta = this.metaData
	val rs = meta.getIndexInfo(this.catalog, this.schema, tableName, false, false)
	rs.dump()
}

fun Connection.tableIndexList(tableName: String): List<IndexInfo> {
	val meta = this.metaData
	val rs = meta.getIndexInfo(this.catalog, this.schema, tableName, false, false)
	return rs.models {
		IndexInfo()
	}
}

inline fun Connection.trans(block: (Connection) -> Unit) {
	try {
		this.autoCommit = false
		block(this)
		this.commit()
	} catch (ex: Exception) {
		this.rollback()
		throw ex
	} finally {
		this.autoCommit = true
	}
}

