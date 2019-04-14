package yet.web.model

import dev.entao.kbase.DefaultValue
import dev.entao.ken.anno.FormOptions
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Model
import dev.entao.sql.ModelClass

/**
 * Created by entaoyang@163.com on 2018/7/11.
 */

class UserClient : Model() {

	@PrimaryKey
	var userId: Int by model


	var clientId: String  by model

	@FormOptions("0:个推", "1:信鸽")
	@DefaultValue("0")
	var pushtype: Int by model


	companion object : ModelClass<UserClient>() {

	}

}