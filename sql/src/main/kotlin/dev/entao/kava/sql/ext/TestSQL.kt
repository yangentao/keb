package dev.entao.kava.sql.ext

import dev.entao.kava.log.*
import dev.entao.kava.sql.*
import java.sql.Connection
import java.sql.ResultSet

val mysqlUrl = "jdbc:mysql://localhost:3306/test?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true"
val mysqlMaker: ConnMaker = MySQLConnMaker(mysqlUrl, "test", "test")

val pgMaker: ConnMaker = PostgreSQLConnMaker("jdbc:postgresql://localhost:5432/yangentao", "yangentao", "")


fun main() {
	ConnLook.maker = pgMaker

//	val ls = ConnLook.defaultConnection.tableIndexList(LogTable::class.sqlName)
//	for(item in ls){
//		logd(item.COLUMN_NAME, item.INDEX_NAME, item.TABLE_NAME, item.TABLE_CAT, item.TYPE)
//	}


	val p = LogTablePrinter()
	Yog.addPrinter(p)

	logd(" a logd msg")

	LogTable.findAll { desc(LogTable::timestamp) }.forEach {
		logd(it.id, it.leveln, it.level, it.tag, it.msg, it.date, it.time, it.timestamp)
	}

//	logi(" a logi msg")
//	loge(" a loge msg")
//
//	logdx("pg", "a logd msg")
//	logix("pg", " a logi msg")
//	logex("pg", " a loge msg")

}