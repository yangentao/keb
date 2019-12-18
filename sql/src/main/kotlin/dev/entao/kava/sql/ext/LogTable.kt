package dev.entao.kava.sql.ext

import dev.entao.kava.base.Label
import dev.entao.kava.base.Length
import dev.entao.kava.log.YogItem
import dev.entao.kava.log.YogLevel
import dev.entao.kava.log.YogPrinter
import dev.entao.kava.sql.*

class LogTable : Model() {

	@Label("ID")
	@AutoInc
	@PrimaryKey
	var id: Long by model

	@Label("LEVELN")
	@Index
	var leveln: Int by model

	@Label("LEVEL")
	@Index
	@Length(64)
	var level: String by model

	@Label("TAG")
	@Index
	@Length(64)
	var tag: String by model

	@Label("Message")
	@Index
	@Length(2048)
	var msg: String by model

	@Index
	var date: java.sql.Date by model

	@Index
	var time: java.sql.Time by model

	@Index
	var timestamp: java.sql.Timestamp by model


	companion object : ModelClass<LogTable>() {

		fun insertFrom(item: YogItem) {
			val m = LogTable()
			m.leveln = item.level.value
			m.level = item.level.toString()
			m.tag = item.tag
			m.msg = item.message
			val tm = System.currentTimeMillis()
			m.date = java.sql.Date(tm)
			m.time = java.sql.Time(tm)
			m.timestamp = java.sql.Timestamp(tm)
			m.insert()
		}
	}
}

class LogTablePrinter : YogPrinter {
	var level: YogLevel = YogLevel.INFO

	override fun printItem(item: YogItem) {
		if (item.level >= this.level) {
			LogTable.insertFrom(item)
		}
	}

}