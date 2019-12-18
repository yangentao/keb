@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.log.loge
import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.userName
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.full.createInstance

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */

open class ModelClass<out T : Model> {

	private val modelClass = javaClass.enclosingClass.kotlin
	//lowercased => columnName
	private val colMap: HashMap<String, String> = HashMap(64)
	private val propMap: HashMap<Prop, String> = HashMap(64)
	private val dbType: Int

	init {
		val d = DefTable(modelClass)
		dbType = d.dbType
		val cs = con.tableDesc(d.name).map { it.COLUMN_NAME }
		for (a in cs) {
			colMap[a.toLowerCase()] = a
		}
	}

	fun col(p: Prop): String {
		return propMap.getOrPut(p) {
			colMap[p.userName.toLowerCase()] ?: p.userName.toLowerCase()
		}
	}

	@Suppress("UNCHECKED_CAST")
	open fun onMapInstance(map: Map<String, Any?>): T {
		val m = modelClass.createInstance() as T
		m.model.putAll(map)
		return m
	}

	val con: Connection get() = modelClass.namedConn

	val sql: SQL get() = SQL(con)

	open fun delete(w: Where?): Int {
		return con.delete(modelClass, w)
	}

	open fun update(map: Map<Prop, Any?>, w: Where?): Int {
		return con.update(modelClass, map, w)
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
			from(modelClass)
			this.block()
		}
	}

	open fun countAll(w: Where?): Int {
		return con.countAll(modelClass, w)
	}

	open fun findAll(block: SQLQuery.() -> Unit): List<T> {
		return findAll(null, block)
	}

	open fun findAll(w: Where?): List<T> {
		return SQL(con).selectAll().from(modelClass).where(w).query().allRows().map { onMapInstance(it) }
	}

	open fun findAll(w: Where?, block: SQLQuery.() -> Unit): List<T> {
		val ls = con.query {
			from(modelClass)
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
		return SQL(con).selectAll().from(modelClass).where(w).limit(1).query().allRows().map { onMapInstance(it) }
				.firstOrNull()
	}

	open fun findOne(w: Where?, block: SQLQuery.() -> Unit): T? {
		return findAll(w) {
			limit(1)
			this.block()
		}.firstOrNull()
	}

	open fun findAll(vararg ps: Pair<Prop1, Any>): List<T> {
		if (ps.isEmpty()) {
			return emptyList()
		}
		var w: Where? = null
		for (p in ps) {
			w = w AND (p.first EQ p.second)
		}
		if (w == null) {
			return emptyList()
		}
		return findAll(w)
	}

	open fun findByKey(pkValue: Any): T? {
		val pks = this.modelClass.modelPrimaryKeys
		if (pks.size != 1) {
			loge("${this::class.qualifiedName} 主键和给的值,数量不匹配")
			return null
		}
		return findOne(pks[0] EQ pkValue)
	}

	open fun dumpTable() {
		con.dump { from(modelClass) }
	}

}