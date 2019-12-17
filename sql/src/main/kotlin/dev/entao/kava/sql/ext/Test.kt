package dev.entao.kava.sql.ext

import dev.entao.kava.sql.AutoInc
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.PrimaryKey
import java.text.DecimalFormat

class Test : Model() {

	@AutoInc
	@PrimaryKey
	var id: Int by model
	var name: String by model

}