package net.yet.web.role

import dev.entao.kbase.Label
import dev.entao.sql.Exclude
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import yet.web.model.Account

@Label("用户角色")
class RoleMember : Model() {

	@Label("角色ID")
	@PrimaryKey
	var roleId: Int by model

	@Label("用户ID")
	@PrimaryKey
	var accountId: Int by model

	@Exclude
	val roleName: String
		get() {
			return Role.findByKey(roleId)?.name ?: ""
		}
	@Exclude
	val account: Account?
		get() {
			return Account.findByKey(accountId)
		}

	companion object : ModelClass<RoleMember>() {

	}
}