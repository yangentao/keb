package dev.entao.keb.biz.model

import dev.entao.kava.base.Label
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.keb.page.FormOptions

@Label("角色")
class Role : Model() {

	@Label("角色ID")
	@dev.entao.kava.sql.PrimaryKey
	@dev.entao.kava.sql.AutoInc
	var id: Int by model

	@Label("角色名")
	@dev.entao.kava.sql.Index
	var name: String by model

	@FormOptions("0:正常","1:禁用")
	@Label("状态")
	@dev.entao.kava.sql.Index
	var status: Int by model

	companion object : ModelClass<Role>() {

	}
}