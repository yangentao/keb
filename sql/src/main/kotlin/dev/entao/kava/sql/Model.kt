@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.base.*
import dev.entao.json.Yson
import dev.entao.json.YsonObject
import java.sql.Connection
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2017/3/31.
 */

open class Model(val model: ModelMap = ModelMap()) {

	private val conn: Connection get() = this::class.namedConn

	fun hasProp(p: KProperty<*>): Boolean {
		return hasProp(p)
	}

	fun hasProp(key: String): Boolean {
		return model.containsKey(key) || model.containsKey(key.toLowerCase())
	}

	fun removeProperty(p: KProperty<*>) {
		model.removeProperty(p)
	}

	fun existByKey(): Boolean {
		val w = this.whereByPrimaryKey ?: throw IllegalArgumentException("必须设置主键")
		val cls = this::class
		return conn.query {
			select("1")
			from(cls)
			where(w)
			limit(1)
		}.existRow
	}

	fun deleteByKey(): Boolean {
		val w = this.whereByPrimaryKey ?: return false
		return conn.delete(this::class, w) > 0
	}

	fun saveByKey(): Boolean {
		return conn.insertOrUpdate(this)
	}

	fun insert(): Boolean {
		return conn.insert(this)
	}

	fun insertOrUpdate(): Boolean {
		return conn.insertOrUpdate(this)
	}

	fun updateByKey(ps: List<KMutableProperty<*>>): Boolean {
		return if (ps.isNotEmpty()) {
			conn.updateByKey(this, ps)
		} else {
			conn.updateByKey(this)
		} > 0
	}

	fun updateByKey(vararg ps: KMutableProperty<*>): Boolean {
		return if (ps.isNotEmpty()) {
			conn.updateByKey(this, ps.toList())
		} else {
			conn.updateByKey(this)
		} > 0
	}


	override fun toString(): String {
		return Yson.toYson(model).toString()
	}

//	//仅包含有值的列, modMap中出现
//	@Exclude
//	val modelPropertiesExists: List<KMutableProperty<*>>
//		get() {
//			return this::class.modelProperties.filter { model.hasProp(it) }
//		}


}

val <T : Model> T.whereByPrimaryKey: Where?
	get() {
		var w: Where? = null
		this::class.modelPrimaryKeys.forEach {
			w = w AND (it EQ it.getValue(this))
		}
		return w
	}

//仅包含有值的列, modMap中出现
val <T : Model> T.modelPropertiesExists: List<KMutableProperty<*>>
	get() {
		return this::class.modelProperties.filter { model.hasProp(it) }
	}


fun <T : Model> T.update(block: (T) -> Unit): Boolean {
	val ls = this.model.gather {
		block(this)
	}
	if (ls.isNotEmpty()) {
		return this.updateByKey(ls)
	}
	return false
}


fun <T : Model> T.json(block: (Prop) -> Boolean): YsonObject {
	val jo = YsonObject()
	for (p in this::class.modelProperties) {
		if (block(p)) {
			jo.any(p.userName, p.getter.call(this))
		}
	}
	return jo
}

fun <T : Model> T.jsonTo(jo: YsonObject, block: (Prop) -> Boolean): YsonObject {
	for (p in this::class.modelProperties) {
		if (block(p)) {
			jo.any(p.userName, p.getter.call(this))
		}
	}
	return jo
}

fun <T : Model> T.jsonClient(): YsonObject {
	val jo = YsonObject()
	for (p in this::class.modelProperties) {
		if (!p.isHideClient) {
			jo.any(p.userName, p.getter.call(this))
		}
	}
	return jo
}

fun <T : Model> T.jsonClientTo(jo: YsonObject): YsonObject {
	for (p in this::class.modelProperties) {
		if (!p.isHideClient) {
			jo.any(p.userName, p.getter.call(this))
		}
	}
	return jo
}

fun <T : Model> T.jsonExclude(vararg ps: Prop): YsonObject {
	if (ps.isEmpty()) {
		return this.jsonClient()
	}
	val keySet = ps.map { it.name }
	return this.json { it.name !in keySet }
}

fun <T : Model> T.jsonExcludeTo(jo: YsonObject, vararg ps: Prop): YsonObject {
	if (ps.isEmpty()) {
		return this.jsonClientTo(jo)
	}
	val keySet = ps.map { it.name }
	return jsonTo(jo) {
		it.name !in keySet
	}
}

fun <T : Model> T.jsonInclude(vararg ps: Prop): YsonObject {
	if (ps.isEmpty()) {
		return this.jsonClient()
	}
	val jo = YsonObject()
	for (p in ps) {
		jo.any(p.userName, p.getter.call(this))
	}
	return jo
}

fun <T : Model> T.jsonIncludeTo(jo: YsonObject, vararg ps: Prop): YsonObject {
	if (ps.isEmpty()) {
		return this.jsonClientTo(jo)
	}
	for (p in ps) {
		jo.any(p.userName, p.getter.call(this))
	}
	return jo
}