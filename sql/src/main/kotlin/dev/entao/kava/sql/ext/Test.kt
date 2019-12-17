package dev.entao.kava.sql.ext

import dev.entao.kava.sql.AutoInc
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.PrimaryKey

class Test : Model() {

	@AutoInc
	@PrimaryKey
	var id: Int by model
	var name: String by model
}