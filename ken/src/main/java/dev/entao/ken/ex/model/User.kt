package dev.entao.ken.ex.model

import dev.entao.kbase.DefaultValue
import dev.entao.kbase.HideClient
import dev.entao.kbase.Label
import dev.entao.kbase.Name
import dev.entao.ken.anno.*
import dev.entao.sql.AutoInc
import dev.entao.sql.EQ
import dev.entao.sql.Index
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Unique

/**
 * Created by entaoyang@163.com on 2018/4/2.
 */
//app 用户
@Label("用户")
@Name("user")
class User : Model() {

	@Label("ID")
	@AutoInc
	@PrimaryKey
	var id: Int by model

	@ColumnWidth("8em")
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

	@DefaultValue("0")
	@FormOptions("0:普通", "1:内部")
	@Label("类型")
	@Index
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