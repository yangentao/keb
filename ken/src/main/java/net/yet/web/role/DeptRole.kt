package net.yet.web.role

import dev.entao.kbase.Label
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Model
import dev.entao.sql.ModelClass

@Label("组角色")
class DeptRole : Model() {


	@Label("组ID")
	@PrimaryKey
	var deptId: Int by model

	@Label("角色ID")
	@PrimaryKey
	var roleId: Int by model

	companion object : ModelClass<DeptRole>() {

	}
}