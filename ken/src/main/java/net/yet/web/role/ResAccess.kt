package net.yet.web.role

import dev.entao.kbase.Label
import dev.entao.ken.anno.FormOptions
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Model
import dev.entao.sql.ModelClass

@Label("访问资源")
class ResAccess : Model() {


	@Label("URI")
	@PrimaryKey
	var uri: String by model

	@PrimaryKey
	@Label("对象")
	var objId: Int by model

	@FormOptions("0:账号", "1:部门", "2:角色")
	@PrimaryKey
	@Label("对象类型")
	var objType: Int by model

	@FormOptions("0:拒绝", "1:允许", "-1:条件")
	@Label("判定")
	var judge: Int by model

	//{"value1":"collegeId", "op":"EQ", "value2":10}
	//EQ , GE, GT, LE, LT, IN, ISNULL, NOTNULL
	//AND, OR
	@Label("条件")
	var condition: String by model

	companion object : ModelClass<ResAccess>() {
		const val Deny = 0
		const val Allow = 1
		const val Cond = -1

		const val TAccount = 0
		const val TDept = 1
		const val TRole = 2



	}

}


