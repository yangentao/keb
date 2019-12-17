@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.kava.sql

import dev.entao.kava.base.Task
import java.sql.Connection
import javax.naming.InitialContext
import javax.naming.NameNotFoundException
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

object ConnLook {
	private const val prefix = "java:comp/env/jdbc"
	private val ctx = InitialContext()
	private val nameList = ArrayList<String>()

	private val threadLocal: ThreadLocal<HashMap<String, Connection>> =
			object : ThreadLocal<HashMap<String, Connection>>() {
				override fun initialValue(): HashMap<String, Connection> {
					return HashMap()
				}
			}

	init {
		try {
			val ls = ctx.list(prefix)
			while (ls.hasMore()) {
				val a = ls.next()
				nameList += a.name
			}
		} catch (ex: NameNotFoundException) {
//            ex.printStackTrace()
		}
		Task.setCleanBlock("connlook", this::cleanThreadConnections)
	}

	fun cleanThreadConnections() {
		val map = threadLocal.get()
		for (c in map.values) {
			c.close()
		}
		map.clear()
		threadLocal.remove()
	}

	fun named(name: String): Connection {
		val map = threadLocal.get()
		return map.getOrPut(name) {
			val defaultDataSource = ctx.lookup("$prefix/$name") as DataSource
			defaultDataSource.connection
		}
	}

	val first: Connection get() = named(nameList.first())

	fun named(modelClass: KClass<*>): Connection {
		val cname = modelClass.findAnnotation<ConnName>() ?: return first
		if (cname.value.isEmpty()) {
			return first
		}
		return named(cname.value)
	}
}

val KClass<*>.namedConn: Connection get() = ConnLook.named(this)