package dev.entao.ken.ex.model

import dev.entao.kbase.DefaultValue
import dev.entao.kbase.Label
import dev.entao.ken.anno.*
import dev.entao.sql.AutoInc
import dev.entao.sql.ForeignKey
import dev.entao.sql.ForeignLabel
import dev.entao.sql.Index
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import dev.entao.sql.PrimaryKey

@Label("部门")
class Dept : Model() {

	@Label("ID")
	@PrimaryKey
	@AutoInc
	var id: Int by model

	@FormRequired
	@Index
	@Label("组名")
	var name: String by model

	@ForeignKey(Dept::class)
	@ForeignLabel("name")
	@DefaultValue("0")
	@Label("上级")
	var parentId: Int by model

	@FormOptions("0:正常", "1:禁用")
	@Label("状态")
	@Index
	var status: Int by model

	@Index
	@Label("主管")
	var leaderId: Int by model

	companion object : ModelClass<Dept>() {

	}
}