@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.base.Prop
import dev.entao.kava.base.isPublic
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */

open class ModelClass<out T : Model> {

	@Suppress("UNCHECKED_CAST")
	private val tabCls: KClass<T> = javaClass.enclosingClass.kotlin as KClass<T>

	init {
		DefTable(tabCls)
	}


	@Suppress("UNCHECKED_CAST")
	open fun mapRow(map: Map<String, Any?>): T {
		val m = tabCls.createInstance()
		m.model.putAll(map)
		return m
	}

	val con: Connection get() = tabCls.namedConn


	fun delete(w: Where, vararg ws: Where): Int {
		return con.delete(tabCls, andW(w, *ws))
	}

	fun update(map: Map<Prop, Any?>, w: Where?): Int {
		return con.update(tabCls, map, w)
	}

	fun update(p: Pair<Prop, Any?>, w: Where?): Int {
		return update(mapOf(p), w)
	}

	fun update(p: Pair<Prop, Any?>, p2: Pair<Prop, Any?>, w: Where?): Int {
		return update(mapOf(p, p2), w)
	}

	fun update(vararg ps: Pair<Prop, Any?>, block: () -> Where?): Int {
		return update(ps.toMap(), block())
	}


	fun dumpTable() {
		con.dump { from(tabCls) }
	}

	fun exits(w: Where, vararg ws: Where): Boolean {
		return query {
			select("1")
			where(andW(w, *ws))
			limit(1)
		}.existRow
	}

	fun oneKey(pkValue: Any): T? {
		val pks = this.tabCls.modelPrimaryKeys
		assert(pks.size == 1)
		return this.one(pks[0] EQ pkValue)
	}

	fun one(w: Where, vararg ws: Where): T? {
		return list {
			where(andW(w, *ws))
			limit(1)
		}.firstOrNull()
	}

	fun asc(prop: Prop, w: Where, vararg ws: Where): List<T> {
		return list {
			where(andW(w, *ws))
			asc(prop)
		}
	}

	fun desc(prop: Prop, w: Where, vararg ws: Where): List<T> {
		return list {
			where(andW(w, *ws))
			desc(prop)
		}
	}

	fun filter(w: Where, vararg ws: Where): List<T> {
		return list {
			where(andW(w, *ws))
		}
	}

	fun list(block: SQLQuery.() -> Unit): List<T> {
		val ls = con.query {
			from(tabCls)
			this.block()
		}.allRows()
		return ls.map { mapRow(it) }
	}

	fun query(block: SQLQuery.() -> Unit): ResultSet {
		return con.query {
			from(tabCls)
			this.block()
		}
	}

	fun count(w: Where, vararg ws: Where): Int {
		return con.countAll(tabCls, andW(w, *ws))
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