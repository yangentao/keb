package dev.entao.kava.sql

import dev.entao.kava.base.*
import java.sql.Connection
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
		return this.insert(a) > 0
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
		return this.insert(a) > 0
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

fun Connection.countAll(cls: TabClass, w: Where?): Int {
	return this.querySQL {
		select("COUNT(*)").from(cls).where(w)
	}.intValue ?: 0
}


fun Connection.update(cls: TabClass, map: Map<Prop, Any?>, w: Where?): Int {
	return this.updateSQL {
		update(cls, map).where(w)
	}
}

fun Connection.delete(cls: TabClass, w: Where?): Int {
	return this.updateSQL {
		deleteFrom(cls).where(w)
	}
}

