package dev.entao.keb.am.model

import dev.entao.kava.base.Length
import dev.entao.kava.sql.*

class Account : Model() {

	@PrimaryKey
	@AutoInc
	var id: Int by model

	@Index
	@Length(32)
	var phone: String by model

	@Length(64)
	var pwd: String by model

	companion object : ModelClass<Account>()
}