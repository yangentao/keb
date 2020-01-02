package dev.entao.kava.sql.ext

import dev.entao.json.*
import dev.entao.kava.base.TextConverts
import dev.entao.kava.base.TimeMill
import dev.entao.kava.log.*
import dev.entao.kava.sql.*

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

	TextConverts[YsonArray::class] = YsonArrayTextConvert
	TextConverts[YsonObject::class] = YsonObjectTextConvert

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
	Test.tableQuery {
		select {
			+Test::id
			+Test::name
			sum(Test::age)
			over {
				partitionBy(Test::name)
				desc(Test::id)
				alias("sumAge")
			}
		}
//		groupBy(Test::name)
		desc(Test::name)
	}.dump()


	println("============")
	val a = TableQuery(Test::class).select(Test::id, Test::name).where(Test::id EQ 1)
	val b = TableQuery(Test::class).select(Test::id, Test::name).where(Test::id EQ 2)
	Test.query(UNION(a, b)).dump()

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