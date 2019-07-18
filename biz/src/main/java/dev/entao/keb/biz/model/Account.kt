package dev.entao.keb.biz.model

import dev.entao.kava.base.DefaultValue
import dev.entao.kava.base.FormDate
import dev.entao.kava.base.HideClient
import dev.entao.kava.base.Label
import dev.entao.kava.sql.EQ
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.keb.page.FormOptions
import dev.entao.keb.page.FormRequired

//账号是指web管理端的账号

@Label("账号")
class Account : Model() {

	@Label("ID")
	@dev.entao.kava.sql.AutoInc
	@dev.entao.kava.sql.PrimaryKey
	var id: Int by model

	@dev.entao.kava.sql.Index
	@dev.entao.kava.sql.Unique
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
	@dev.entao.kava.sql.Index
	var status: Int by model

	@FormDate("MM-dd HH:mm")
	@Label("最后登录时间")
	var lastLogin: Long by model

	@Label("最后IP")
	var lastIp: String by model

	@Label("部门")
	@DefaultValue("0")
	@dev.entao.kava.sql.ForeignKey(Dept::class)
	@dev.entao.kava.sql.ForeignLabel("name")
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