package dev.entao.kava.sql

class Test : Model() {

	@dev.entao.kava.sql.AutoInc
	@dev.entao.kava.sql.PrimaryKey
	var id: Int by model
	var name: String by model
}