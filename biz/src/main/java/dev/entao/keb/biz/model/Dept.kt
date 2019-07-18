package dev.entao.keb.biz.model

import dev.entao.kava.base.DefaultValue
import dev.entao.kava.base.Label
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.keb.page.FormOptions
import dev.entao.keb.page.FormRequired

@Label("部门")
class Dept : Model() {

	@Label("ID")
	@dev.entao.kava.sql.PrimaryKey
	@dev.entao.kava.sql.AutoInc
	var id: Int by model

	@FormRequired
	@dev.entao.kava.sql.Index
	@Label("组名")
	var name: String by model

	@dev.entao.kava.sql.ForeignKey(Dept::class)
	@dev.entao.kava.sql.ForeignLabel("name")
	@DefaultValue("0")
	@Label("上级")
	var parentId: Int by model

	@FormOptions("0:正常", "1:禁用")
	@Label("状态")
	@dev.entao.kava.sql.Index
	var status: Int by model

	@dev.entao.kava.sql.Index
	@Label("主管")
	var leaderId: Int by model

	companion object : ModelClass<Dept>() {

	}
}