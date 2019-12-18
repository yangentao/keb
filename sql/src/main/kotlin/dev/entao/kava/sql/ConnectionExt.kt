package dev.entao.kava.sql

import dev.entao.kava.json.*
import dev.entao.kava.base.getValue
import dev.entao.kava.base.hasAnnotation
import dev.entao.kava.base.isTypeLong
import dev.entao.kava.base.setValue
import dev.entao.kava.log.logd
import dev.entao.kava.base.Prop
import java.lang.IllegalArgumentException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */
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

//现有记录和要插入的记录完全一样, 也会返回false, 表示没有更新
fun Connection.insertOrUpdate(model: Model): Boolean {
	val cs = model.modelPropertiesExists
	var autoInc = false
	for (pk in model::class.modelPrimaryKeys) {
		if (pk.hasAnnotation<AutoInc>()) {
			autoInc = true
		}
		if (pk !in cs) {
			throw IllegalArgumentException("insertOrUpdate 必须包含主键的值")
		}
	}
	val a = SQL()
	if (this.isMySQL) {
		a.insertOrUpdateMySqL(model::class, cs.map { it to it.getValue(model) }, model::class.modelPrimaryKeys)
	} else if (this.isPostgres) {
		a.insertOrUpdatePG(model::class, cs.map { it to it.getValue(model) }, model::class.modelPrimaryKeys)
	}
	if (!autoInc) {
		return this.insertSQL(a) > 0
	}
	val lVal = this.insertSQLGenKey(a)
	if (lVal <= 0L) {
		return false
	}
	val pkProp = model::class.modelPrimaryKeys.first { it.hasAnnotation<AutoInc>() }
	if (pkProp.returnType.isTypeLong) {
		pkProp.setValue(model, lVal)
	} else {
		pkProp.setValue(model, lVal.toInt())
	}
	return true
}

fun Connection.insert(model: Model): Boolean {
	val autoInc = model::class.modelPrimaryKeys.find { it.hasAnnotation<AutoInc>() } != null
	val a = SQL()
	a.insert(model::class.s, model.modelPropertiesExists.map { it.s to it.getValue(model) })
	if (!autoInc) {
		return this.insertSQL(a) > 0
	}
	val lVal = this.insertSQLGenKey(a)
	if (lVal <= 0L) {
		return false
	}
	val pkProp = model::class.modelPrimaryKeys.first { it.hasAnnotation<AutoInc>() }
	if (pkProp.returnType.isTypeLong) {
		pkProp.setValue(model, lVal)
	} else {
		pkProp.setValue(model, lVal.toInt())
	}
	return true
}


fun Connection.update(sql: String, args: List<Any?> = emptyList()): Int {
	if (ConnLook.logEnable) {
		logd(sql)
		logd(args)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	val n = st.executeUpdate()
	st.close()
	return n
}

fun Connection.insertGenKey(sql: String, args: ArrayList<Any?>): Long {
	val st = this.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
	st.setParams(args)
	if (ConnLook.logEnable) {
		logd(sql)
		logd(args)
	}
	val n = st.executeUpdate()
	val r: Long = if (n <= 0) {
		0L
	} else {
		st.generatedKeys.longValue ?: 0L
	}
	st.close()
	return r
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

fun Connection.query(q: SQLQuery): ResultSet {
	return this.query(q.toSQL(), q.args)
}

fun Connection.query(block: SQLQuery.() -> Unit): ResultSet {
	val q = SQLQuery()
	q.block()
	return this.query(q.toSQL(), q.args)
}

fun Connection.countAll(cls: KClass<*>, w: Where?): Int {
	return this.querySQL {
		select("COUNT(*)").from(cls).where(w)
	}.intValue ?: 0
}

fun Connection.dump(block: SQLQuery.() -> Unit) {
	val q = SQLQuery()
	q.block()
	val sql = q.toSQL()
	this.query(sql, emptyList()).dump()
}

fun Connection.updateByKey(model: Model, ps: List<KMutableProperty<*>> = model.modelPropertiesExists): Boolean {
	val pkList = model::class.modelPrimaryKeys
	if (pkList.isEmpty()) {
		throw IllegalArgumentException("updateByKey, 必须定义主键")
	}
	val ls = ps.filter { it !in pkList }
	return this.updateSQL {
		update(model::class.s, ls.map {
			it.s to it.getValue(model)
		}).where(model.whereByPrimaryKey)
	} > 0
}

fun Connection.update(cls: KClass<*>, map: Map<Prop, Any?>, w: Where?): Int {
	return this.updateSQL {
		update(cls, map).where(w)
	}
}

fun Connection.delete(cls: KClass<*>, w: Where?): Int {
	return this.updateSQL {
		deleteFrom(cls).where(w)
	}
}

fun Connection.exec(sql: String, args: List<Any> = emptyList()): Boolean {
	if (ConnLook.logEnable) {
		logd(sql)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	return st.use {
		st.execute()
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

