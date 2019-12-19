package dev.entao.kava.sql.ext

import dev.entao.kava.sql.AutoInc
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.kava.sql.PrimaryKey

class User : Model() {
	@PrimaryKey
	@AutoInc
	var id: Int by model

	var cache: String by model

	var abort: Int by model

	companion object : ModelClass<User>()
}