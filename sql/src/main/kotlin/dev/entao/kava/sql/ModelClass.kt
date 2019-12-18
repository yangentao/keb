@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.log.loge
import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.userName
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

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
	open fun onMapInstance(map: Map<String, Any?>): T {
		val m = tabCls.createInstance() as T
		m.model.putAll(map)
		return m
	}

	val con: Connection get() = tabCls.namedConn


	open fun delete(w: Where?): Int {
		return con.delete(tabCls, w)
	}

	open fun update(map: Map<Prop, Any?>, w: Where?): Int {
		return con.update(tabCls, map, w)
	}

	open fun update(p: Pair<Prop, Any?>, w: Where?): Int {
		return update(mapOf(p), w)
	}

	open fun update(p: Pair<Prop, Any?>, p2: Pair<Prop, Any?>, w: Where?): Int {
		return update(mapOf(p, p2), w)
	}

	open fun update(vararg ps: Pair<Prop, Any?>, block: () -> Where?): Int {
		return update(ps.toMap(), block())
	}

	open fun query(block: SQLQuery.() -> Unit): ResultSet {
		return con.query {
			from(tabCls)
			this.block()
		}
	}

	open fun countAll(w: Where?): Int {
		return con.countAll(tabCls, w)
	}

	open fun findAll(block: SQLQuery.() -> Unit): List<T> {
		return findAll(null, block)
	}

	open fun findAll(w: Where?): List<T> {
		return con.querySQL {
			selectAll().from(tabCls).where(w)
		}.allRows().map { onMapInstance(it) }
	}

	open fun findAll(w: Where?, block: SQLQuery.() -> Unit): List<T> {
		val ls = con.query {
			from(tabCls)
			if (w != null) {
				where(w)
			}
			this.block()
		}.allRows()
		return ls.map { onMapInstance(it) }
	}

	open fun findAll(): List<T> {
		return findAll(null)
	}

	open fun findOne(w: Where?): T? {
		return con.querySQL {
			selectAll().from(tabCls).where(w).limit(1)
		}.allRows().map { onMapInstance(it) }.firstOrNull()
	}

	open fun findOne(w: Where?, block: SQLQuery.() -> Unit): T? {
		return findAll(w) {
			limit(1)
			this.block()
		}.firstOrNull()
	}

	open fun findAll(p: Pair<Prop, Any>, vararg ps: Pair<Prop1, Any>): List<T> {
		var w: Where = p.first EQ p.second
		for (a in ps) {
			w = w AND (a.first EQ a.second)
		}
		return findAll(w)
	}

	open fun findByKey(pkValue: Any): T? {
		val pks = this.tabCls.modelPrimaryKeys
		if (pks.size != 1) {
			loge("${this::class.qualifiedName} 主键和给的值,数量不匹配")
			return null
		}
		return findOne(pks[0] EQ pkValue)
	}

	open fun findByKeys(vararg keys: Any): T? {
		val pks = this.tabCls.modelPrimaryKeys
		assert(keys.isNotEmpty() && pks.size == keys.size)
		var w: Where? = null
		for (i in pks.indices) {
			w = w AND (pks[i] EQ keys[i])
		}
		return findOne(w)
	}

	open fun dumpTable() {
		con.dump { from(tabCls) }
	}

}