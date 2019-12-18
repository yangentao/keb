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

