package dev.entao.keb.biz.model

import dev.entao.kava.base.Label
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass

@Label("组角色")
class DeptRole : Model() {


	@Label("组ID")
	@dev.entao.kava.sql.PrimaryKey
	var deptId: Int by model

	@Label("角色ID")
	@dev.entao.kava.sql.PrimaryKey
	var roleId: Int by model

	companion object : ModelClass<DeptRole>() {

	}
}