package dev.entao.speak.model

import dev.entao.kava.base.Length
import dev.entao.kava.sql.*
import dev.entao.keb.page.FormOptions

class UserDir : Model() {

	@PrimaryKey
	@AutoInc
	var id: Long by model

	@Index
	@Length(32)
	var dirName: String by model

	@Length(64)
	var userId: Long by model

	@FormOptions("0:Normal", "1:Deleted")
	var flag: Int by model

	companion object : ModelClass<UserDir>()
}