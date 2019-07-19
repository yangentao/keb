package dev.entao.keb.biz.model

import dev.entao.kava.base.DefaultValue
import dev.entao.kava.base.HideClient
import dev.entao.kava.base.Label
import dev.entao.kava.base.Name
import dev.entao.kava.sql.EQ
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.keb.page.ColumnWidth
import dev.entao.keb.page.FormOptions
import dev.entao.keb.page.FormRequired

/**
 * Created by entaoyang@163.com on 2018/4/2.
 */
//app 用户
@Label("用户")
@Name("user")
class User : Model() {

	@Label("ID")
	@dev.entao.kava.sql.AutoInc
	@dev.entao.kava.sql.PrimaryKey
	var id: Int by model

	@ColumnWidth("8em")
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

	@DefaultValue("0")
	@FormOptions("0:普通", "1:内部")
	@Label("类型")
	@dev.entao.kava.sql.Index
	var userType: Int by model

	var lastLogin: Long by model
	var lastTime: Long by model

	companion object : ModelClass<User>() {

		fun nameOf(uid: Int): String? {
			if (uid > 0) {
				return User.findByKey(uid)?.name
			} else {
				return null
			}
		}

		fun findByPhone(phone: String): User? {
			return User.findOne(User::phone EQ phone)
		}
	}

}