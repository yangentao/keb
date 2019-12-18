package dev.entao.kava.sql.ext

import dev.entao.kava.sql.*

class Test : Model() {

	@AutoInc
	@PrimaryKey
	var id: Int by model
	var name: String by model

	var user: String by model

	@Decimal(6, 3)
	var fee: Double by model

	companion object : ModelClass<Test>()

}