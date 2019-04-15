package dev.entao.biz.model

import dev.entao.kava.base.Label
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass

@Label("用户角色")
class RoleMember : Model() {

	@Label("角色ID")
	@dev.entao.kava.sql.PrimaryKey
	var roleId: Int by model

	@Label("用户ID")
	@dev.entao.kava.sql.PrimaryKey
	var accountId: Int by model

	@dev.entao.kava.sql.Exclude
	val roleName: String
		get() {
			return Role.findByKey(roleId)?.name ?: ""
		}
	@dev.entao.kava.sql.Exclude
	val account: Account?
		get() {
			return Account.findByKey(accountId)
		}

	companion object : ModelClass<RoleMember>() {

	}
}