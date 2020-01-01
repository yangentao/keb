@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.isPublic
import dev.entao.kava.base.ownerClass
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */

open class ModelClass<T : Model> {

	@Suppress("UNCHECKED_CAST")
	val tableClass: KClass<T> = javaClass.enclosingClass.kotlin as KClass<T>
	val con: Connection get() = tableClass.namedConn

	init {
		DefTable(tableClass)
	}


	@Suppress("UNCHECKED_CAST")
	open fun mapRow(map: Map<String, Any?>): T {
		val m = tableClass.createInstance()
		m.model.putAll(map)
		return m
	}


	fun delete(w: Where, vararg ws: Where): Int {
		return con.delete(tableClass, andW(w, *ws))
	}

	fun deleteByKey(keyValue: Any): Int {
		return delete(keyWhere(keyValue))
	}

	fun updateByKey(keyValue: Any, vararg ps: Pair<Prop, Any?>): Int {
		return con.update(tableClass, ps.toList(), keyWhere(keyValue))
	}

	fun update(map: Map<Prop, Any?>, w: Where?): Int {
		return con.update(tableClass, map.map { it.key to it.value }, w)
	}

	fun update(p: Pair<Prop, Any?>, w: Where?): Int {
		return con.update(tableClass, listOf(p), w)
	}

	fun update(p: Pair<Prop, Any?>, p2: Pair<Prop, Any?>, w: Where?): Int {
		return con.update(tableClass, listOf(p, p2), w)
	}

	fun update(vararg ps: Pair<Prop, Any?>, block: () -> Where?): Int {
		return con.update(tableClass, ps.toList(), block())
	}

	fun keyWhere(pkValue: Any): Where {
		val pks = tableClass.modelPrimaryKeys
		assert(pks.size == 1)
		return pks.first() EQ pkValue
	}

	fun dumpTable() {
		con.dump { from(tableClass) }
	}

	fun exits(w: Where, vararg ws: Where): Boolean {
		return query {
			select("1")
			where(andW(w, *ws))
			limit(1)
		}.existRow
	}

	fun oneKey(pkValue: Any, block: TableQuery.() -> Unit = {}): T? {
		return this.one(keyWhere(pkValue), block = block)
	}

	fun one(w: Where, vararg ws: Where, block: TableQuery.() -> Unit = {}): T? {
		return filter(w, *ws) {
			limit(1)
			this.block()
		}.firstOrNull()
	}


	fun filter(w: Where, vararg ws: Where, block: TableQuery.() -> Unit): List<T> {
		return list {
			where(andW(w, *ws))
			this.block()
		}
	}

	fun list(block: TableQuery.() -> Unit): List<T> {
		val ls = tableQuery(block).allMaps
		return ls.map { mapRow(it) }
	}

	//单表
	fun tableQuery(block: TableQuery.() -> Unit): ResultSet {
		val q = TableQuery(tableClass.sqlName)
		q.block()
		return con.query(q.toSQL(), q.args)
	}

	fun query(block: SQLQuery.() -> Unit): ResultSet {
		return con.query {
			from(tableClass)
			this.block()
		}
	}

	fun query(sa: SQLArgs): ResultSet {
		return con.query(sa)
	}


	fun count(w: Where?, vararg ws: Where): Int {
		var ww = w
		for (a in ws) {
			ww = ww AND a
		}
		return con.countAll(tableClass, ww)
	}

	fun joinOn(thisColumnOn: Prop1, otherColumnOn: Prop1, where: Where): List<T> {
		return this.query {
			select("${tableClass.sqlName}.*")
			join(otherColumnOn.ownerClass!!)
			on { thisColumnOn EQ otherColumnOn }
			where(where)
		}.allMaps.map {
			this.mapRow(it)
		}
	}
}


val KClass<*>.modelProperties: List<KMutableProperty<*>>
	get() {
		return classPropCache.getOrPut(this) {
			findModelProperties(this)
		}
	}

val KClass<*>.modelPrimaryKeys: List<KMutableProperty<*>>
	get() {
		return this.modelProperties.filter {
			it.isPrimaryKey
		}
	}

private val classPropCache = HashMap<KClass<*>, List<KMutableProperty<*>>>(64)

private fun findModelProperties(cls: KClass<*>): List<KMutableProperty<*>> {
	return cls.memberProperties.filter {
		if (it !is KMutableProperty<*>) {
			false
		} else if (it.isAbstract || it.isConst || it.isLateinit) {
			false
		} else if (!it.isPublic) {
			false
		} else !it.isExcluded
	}.map { it as KMutableProperty<*> }
}


