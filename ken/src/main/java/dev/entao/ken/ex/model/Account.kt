package dev.entao.ken.ex.model

import dev.entao.kbase.DefaultValue
import dev.entao.kbase.FormDate
import dev.entao.kbase.HideClient
import dev.entao.kbase.Label
import dev.entao.ken.anno.*
import dev.entao.sql.AutoInc
import dev.entao.sql.EQ
import dev.entao.sql.ForeignKey
import dev.entao.sql.ForeignLabel
import dev.entao.sql.Index
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Unique

//账号是指web管理端的账号

@Label("账号")
class Account : Model() {

	@Label("ID")
	@AutoInc
	@PrimaryKey
	var id: Int by model

	@Index
	@Unique
	@Label("手机号")
	@FormRequired
	var phone: String by model

	@Label("姓名")
	@FormRequired
	var name: String by model

	@HideClient
	@Label("密码")
	@FormRequired
	var pwd: String by model

	@FormOptions("0:正常", "1:禁用")
	@Label("状态")
	@Index
	var status: Int by model

	@FormDate("MM-dd HH:mm")
	@Label("最后登录时间")
	var lastLogin: Long by model

	@Label("最后IP")
	var lastIp: String by model

	@Label("部门")
	@DefaultValue("0")
	@ForeignKey(Dept::class)
	@ForeignLabel("name")
	var deptId: Int by model

	companion object : ModelClass<Account>() {
		const val Enabled = 0
		const val Disabled = 1

		fun nameOf(uid: Int): String? {
			if (uid > 0) {
				return Account.findByKey(uid)?.name
			} else {
				return null
			}
		}

		fun findByPhone(phone: String): Account? {
			return Account.findOne(Account::phone EQ phone)
		}
	}

}