package dev.entao.kava.sql.ext

import dev.entao.kava.base.TextConverts
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

	val con = ConnLook.defaultConnection
	con.dropTable("test")


	val t = Test()
	t.name = "aaa"
	t.msgs = ysonArray(999, 88, 77)
	t.insert()

	Test.dumpTable()


}