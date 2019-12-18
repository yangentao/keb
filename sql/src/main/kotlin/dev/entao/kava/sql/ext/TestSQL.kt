package dev.entao.kava.sql.ext

import dev.entao.kava.log.logd
import dev.entao.kava.sql.*
import java.sql.Connection
import java.sql.ResultSet

val mysqlUrl = "jdbc:mysql://localhost:3306/test?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true"
val mysqlMaker: ConnMaker = MySQLConnMaker(mysqlUrl, "test", "test")

val pgMaker: ConnMaker = PostgreSQLConnMaker("jdbc:postgresql://localhost:5432/yangentao", "yangentao", "")


fun main() {
	ConnLook.maker = pgMaker

	val a = Test.findByKey(2)
	a?.update {
		it.fee = 99.888
	}

	val ls = Test.findAll()
	for (b in ls) {
		logd(b.id, b.name, b.fee)
	}
}