package yet.web.model

import dev.entao.sql.PrimaryKey
import dev.entao.sql.Model
import dev.entao.sql.ModelClass

/**
 * Created by entaoyang@163.com on 2018/7/31.
 */

class Province : Model() {

	@PrimaryKey
	var id: String by model


	var name: String by model


	companion object : ModelClass<Province>() {

	}
}