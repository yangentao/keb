package dev.entao.kava.sql.ext

import dev.entao.kava.base.TextConverts
import dev.entao.kava.base.TimeMill
import dev.entao.kava.json.*
import dev.entao.kava.log.*
import dev.entao.kava.sql.*
import org.postgresql.util.PGobject
import java.sql.Connection
import java.sql.ResultSet

//val mysqlUrl = "jdbc:mysql://localhost:3306/test?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true"
//val mysqlMaker: ConnMaker = MySQLConnMaker(mysqlUrl, "test", "test")

val pgMaker: ConnMaker = PostgreSQLConnMaker("jdbc:postgresql://localhost:5432/yangentao", "yangentao", "")

fun printX(vararg vs: Any?) {
	val s = vs.joinToString(" ") {
		it?.toString() ?: "null"
	}
	println(s)
}

fun main() {
	ConnLook.logEnable = true
	ConnLook.maker = pgMaker

	TextConverts[YsonArray::class] = YsonArrayText
	TextConverts[YsonObject::class] = YsonObjectText

	val m = Test()
	m.name = "Name $TimeMill"
	m.fee = 990.9
	m.age = 88
	m.msgs = ysonArray("a", "b", "c")
	m.insert()


	val jarr = Test.tableQuery { desc(Test::id) }.allJson
	logd(jarr.toString())

	val yo = Test.tableQuery { }.firstObject
	logd(yo)

//	rs.closeAfter {
//		if (rs.next()) {
//			for (i in 1..meta.columnCount) {
//				val label = meta.getColumnLabel(i)
//				val value: Any? = rs.getObject(i)
//				val tname = meta.getColumnTypeName(i)
//				val t = meta.getColumnType(i)
//				printX(t, tname, label, value)
//			}
//		}
//	}


}