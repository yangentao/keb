@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.kava.sql

import dev.entao.kava.base.Task
import java.sql.Connection
import java.sql.DriverManager
import javax.naming.InitialContext
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

interface ConnMaker {
	fun namedConnection(name: String): Connection
}

class MySQLConnMaker(val url: String, val user: String, val pwd: String) : ConnMaker {
	private val driverName = "com.mysql.cj.jdbc.Driver"

	init {
		assert("mysql" in url)
	}

	@Throws
	override fun namedConnection(name: String): Connection {
		Class.forName(driverName)
		return DriverManager.getConnection(url, user, pwd)
	}
}

class PostgreSQLConnMaker(val url: String, val user: String, val pwd: String) : ConnMaker {
	private val driverName = "org.postgresql.Driver"

	init {
		assert("postgresql" in url)
	}


	@Throws
	override fun namedConnection(name: String): Connection {
		Class.forName(driverName)
		return DriverManager.getConnection(url, user, pwd)
	}
}

class WebContextConnMaker : ConnMaker {
	private val prefix = "java:comp/env/jdbc"
	private val ctx = InitialContext()
	private val nameList = ArrayList<String>()

	init {
		val ls = ctx.list(prefix)
		while (ls.hasMore()) {
			val a = ls.next()
			nameList += a.name
		}
	}

	override fun namedConnection(name: String): Connection {
		val n = if (name.isEmpty()) {
			nameList.first()
		} else {
			name
		}
		val defaultDataSource = ctx.lookup("${prefix}/$n") as DataSource
		return defaultDataSource.connection
	}

}


//先赋值maker, 后使用
object ConnLook {
	lateinit var maker: ConnMaker
	var logEnable = true

	private val threadLocal: ThreadLocal<HashMap<String, Connection>> =
			object : ThreadLocal<HashMap<String, Connection>>() {
				override fun initialValue(): HashMap<String, Connection> {
					return HashMap()
				}
			}

	init {
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
			maker.namedConnection(name)
		}
	}

	val defaultConnection: Connection get() = named("")

	fun named(modelClass: KClass<*>): Connection {
		val cname = modelClass.findAnnotation<ConnName>()
		if (cname == null || cname.value.isEmpty()) {
			return defaultConnection
		}
		return named(cname.value)
	}
}

val KClass<*>.namedConn: Connection get() = ConnLook.named(this)