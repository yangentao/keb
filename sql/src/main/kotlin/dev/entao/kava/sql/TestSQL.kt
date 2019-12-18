package dev.entao.kava.sql

import dev.entao.kava.log.logd
import dev.entao.kava.sql.ext.Test
import java.sql.Connection
import java.sql.ResultSet

val mysqlUrl = "jdbc:mysql://localhost:3306/test?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true"
val mysqlMaker: ConnMaker = MySQLConnMaker(mysqlUrl, "test", "test")

val pgMaker: ConnMaker = PostgreSQLConnMaker("jdbc:postgresql://localhost:5432/yangentao", "yangentao", "")

fun useMySQL(block: Connection.() -> Unit) {
	val con = mysqlMaker.defaultConnection()
	con.use(block)
	ConnLook.maker = mysqlMaker
}

fun usePostgres(block: Connection.() -> Unit) {
	val con = pgMaker.defaultConnection()
	con.use(block)
	ConnLook.maker = pgMaker
}

fun testQuery() {
	useMySQL {
		val ls = SQL(this).selectAll().from(Test::class).where(Test::id LE 11).query().allRows().map {
			val t = Test()
			t.model.putAll(it)
			t
		}

		for (t in ls) {
			logd(t.id, t.name)
		}
	}
}

fun testInsert() {
	useMySQL {
		this.createTable("test", listOf("id int primary key AUTO_INCREMENT", "name varchar(256)"))
		for (i in 1..20) {
			val m = Test()
			m.name = "Name $i "
			this.insert(m)
		}
	}
}

fun dumpResultSets(r: ResultSet) {
	val meta = r.metaData
	val colCount = meta.columnCount
	r.closeAfter {
		while (r.next()) {
			for (i in 1..meta.columnCount) {
				logd("CAT:", meta.getCatalogName(i), "SCM:", meta.getSchemaName(i), "TAB:", meta.getTableName(i),
						"COL", meta.getColumnName(i), "COLLB:", meta.getColumnLabel(i),
						"CLS:", meta.getColumnClassName(i), "TYP:", meta.getColumnTypeName(i), "VAL:", r.getObject(i))
			}
		}
	}
}

fun main() {
//	useMySQL {
//		val md = this.metaData
//		logd(md.databaseProductName)
//		logd(md.driverName)
//		val r = SQL(this).select("test.id", "test.name as testName").from(Test::class).query()
//		dumpResultSets(r)
//	}

//	useMySQL {
//		logd(this.metaData.databaseProductName)
//		val r = this.metaData.sqlKeywords
//		logd(r)
////		r.dump()
//	}
	ConnLook.maker = pgMaker
	val m = Test()
	m.name = "yang"
	m.insert()

	val r = Test.con.query("select test.id, test.name,test.user from test", emptyList())
	r.dump()
}