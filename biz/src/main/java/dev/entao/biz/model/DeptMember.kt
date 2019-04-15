package dev.entao.biz.model

import dev.entao.kava.base.Label
import dev.entao.sql.Index
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Model
import dev.entao.sql.ModelClass

@Label("部门成员")
class DeptMember : Model() {

	@Label("部门")
	@PrimaryKey
	var deptId: Int by model

	@Label("账号")
	@PrimaryKey
	var accountId: Int by model



	@Index
	@Label("状态")
	var status: Int by model

	companion object : ModelClass<DeptMember>() {

	}
}