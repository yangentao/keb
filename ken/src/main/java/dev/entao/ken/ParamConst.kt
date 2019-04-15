package dev.entao.ken

import dev.entao.kava.base.userName
import kotlin.reflect.KProperty

object ParamConst {
	const val BACK_URL = "backurl"
	const val ERROR = "errorMsg"
	const val SUCCESS = "successMsg"

	private const val _ERROR = "_error_"

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