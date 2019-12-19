package dev.entao.kava.sql.ext

import dev.entao.kava.log.*
import dev.entao.kava.sql.*
import java.sql.Connection
import java.sql.ResultSet

val mysqlUrl = "jdbc:mysql://localhost:3306/test?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true"
val mysqlMaker: ConnMaker = MySQLConnMaker(mysqlUrl, "test", "test")

val pgMaker: ConnMaker = PostgreSQLConnMaker("jdbc:postgresql://localhost:5432/yangentao", "yangentao", "")


fun main() {
	ConnLook.logEnable = true
	ConnLook.maker = pgMaker


	val m = Test()
	m.id = 1
	m.name = "Hello"
	m.updateByKey()
	val x = Test.findByKey(1)
	logd(x)

}