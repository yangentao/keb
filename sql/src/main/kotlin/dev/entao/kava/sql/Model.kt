@file:Suppress("unused")

package dev.entao.kava.sql

import dev.entao.kava.base.*
import dev.entao.kava.json.Yson
import dev.entao.kava.json.YsonObject
import java.sql.Connection
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2017/3/31.
 */

open class Model(val model: ModelMap = ModelMap()) {

    fun hasProp(p: KProperty<*>): Boolean {
        return hasProp(p.userName)
    }

    fun hasProp(key: String): Boolean {
        return model.containsKey(key)
    }

    fun removeProperty(p: KProperty<*>) {
        model.removeProperty(p)
    }

    fun existRowByKey(): Boolean {
        val w = this.whereByPrimaryKey ?: throw IllegalArgumentException("必须设置主键")
        return SQL(conn).selectAll().from(this::class).where(w).limit(1).query().existRow
    }

    private val conn: Connection get() = ConnLook.named(this::class)

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

    fun replace(): Boolean {
        return conn.replace(this)
    }

    fun insertOrUpdate(): Boolean {
        return conn.insertOrUpdate(this)
    }

    fun updateByKey(ps: List<KMutableProperty<*>>): Boolean {
        return if (ps.isNotEmpty()) {
            conn.updateByKey(this, ps)
        } else {
            conn.updateByKey(this)
        }
    }

    fun updateByKey(vararg ps: KMutableProperty<*>): Boolean {
        return if (ps.isNotEmpty()) {
            conn.updateByKey(this, ps.toList())
        } else {
            conn.updateByKey(this)
        }
    }

    fun updateByKey(block: () -> Unit): Boolean {
        val ls = this.model.gather(block)
        if (ls.isNotEmpty()) {
            return this.updateByKey(ls)
        }
        return false
    }

    fun toJsonClient(vararg ps: KProperty<*>): YsonObject {
        val jo = YsonObject()
        if (ps.isEmpty()) {
            for (p in this::class.modelProperties) {
                if (!p.isHideClient) {
                    jo.any(p.userName, p.getter.call(this))
                }
            }
        } else {
            for (p in ps) {
                jo.any(p.userName, p.getter.call(this))
            }
        }
        return jo
    }

    fun toJson(vararg ps: KProperty<*>): YsonObject {
        val jo = YsonObject()
        if (ps.isEmpty()) {
            for (p in this::class.modelProperties) {
                jo.any(p.userName, p.getter.call(this))
            }
        } else {
            for (p in ps) {
                jo.any(p.userName, p.getter.call(this))
            }
        }
        return jo
    }

    fun fillJsonClient(jo: YsonObject, vararg ps: KProperty<*>): YsonObject {
        if (ps.isEmpty()) {
            for (p in this::class.modelProperties) {
                if (!p.isHideClient) {
                    val v = p.getter.call(this)
                    jo.any(p.userName, v)
                }
            }
        } else {
            for (p in ps) {
                val v = p.getter.call(this)
                jo.any(p.userName, v)
            }
        }
        return jo
    }

    fun fillJson(jo: YsonObject, vararg ps: KProperty<*>): YsonObject {
        if (ps.isEmpty()) {
            for (p in this::class.modelProperties) {
                val v = p.getter.call(this)
                jo.any(p.userName, v)
            }
        } else {
            for (p in ps) {
                val v = p.getter.call(this)
                jo.any(p.userName, v)
            }
        }
        return jo
    }

    override fun toString(): String {
        return Yson.toYson(model).toString()
    }

    //仅包含有值的列, modMap中出现
    @Exclude
    val modelPropertiesExists: List<KMutableProperty<*>>
        get() {
            return this::class.modelProperties.filter { model.containsKey(it.userName) }
        }

    @Exclude
    val whereByPrimaryKey: Where?
        get() {
            var w: Where? = null
            this::class.modelPrimaryKeys.forEach {
                w = w AND (it EQ it.getValue(this))
            }
            return w
        }

}