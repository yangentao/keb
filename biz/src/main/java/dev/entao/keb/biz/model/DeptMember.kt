package dev.entao.keb.biz.model

import dev.entao.kava.base.Label
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass

@Label("部门成员")
class DeptMember : Model() {

	@Label("部门")
	@dev.entao.kava.sql.PrimaryKey
	var deptId: Int by model

	@Label("账号")
	@dev.entao.kava.sql.PrimaryKey
	var accountId: Int by model



	@dev.entao.kava.sql.Index
	@Label("状态")
	var status: Int by model

	companion object : ModelClass<DeptMember>() {

	}
}