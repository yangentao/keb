package dev.entao.kage

import dev.entao.kbase.userName
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2017/6/20.
 */

//应用参数
object P {

	const val pageSize = 50

	//是否倒序
	const val dataDesc = "data-desc"
	//排序字段名
	const val dataSortCol = "data-sortcol"
	const val dataPage = "data-page"

	//不能变, my.js中是固定的
	const val pageN = "p"
	//不能变, my.js中是固定的
	const val ascKey = "asc_key"
	//不能变, my.js中是固定的
	const val descKey = "desc_key"

	const val ERROR = "errorMsg"
	const val SUCCESS = "successMsg"

	private const val _ERROR = "_error_"

	const val QUERY_FORM = "queryForm"

	const val BACK_URL = "backurl"

	fun err(pname: String): String {
		return _ERROR + pname
	}

	fun err(p: KProperty<*>): String {
		return _ERROR + p.userName
	}

	fun isErr(key: String): Boolean {
		return key.startsWith(_ERROR)
	}
}