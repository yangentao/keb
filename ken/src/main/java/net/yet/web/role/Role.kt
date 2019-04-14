package net.yet.web.role

import dev.entao.kbase.Label
import dev.entao.ken.anno.*
import dev.entao.sql.AutoInc
import dev.entao.sql.Index
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import dev.entao.sql.PrimaryKey

@Label("角色")
class Role : Model() {

	@Label("角色ID")
	@PrimaryKey
	@AutoInc
	var id: Int by model

	@Label("角色名")
	@Index
	var name: String by model

	@FormOptions("0:正常","1:禁用")
	@Label("状态")
	@Index
	var status: Int by model

	companion object : ModelClass<Role>() {

	}
}