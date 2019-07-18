package dev.entao.keb.biz.model

import dev.entao.kava.base.DefaultValue
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.keb.page.FormOptions

/**
 * Created by entaoyang@163.com on 2018/7/11.
 */

class UserClient : Model() {

	@dev.entao.kava.sql.PrimaryKey
	var userId: Int by model


	var clientId: String  by model

	@FormOptions("0:个推", "1:信鸽")
	@DefaultValue("0")
	var pushtype: Int by model


	companion object : ModelClass<UserClient>() {

	}

}