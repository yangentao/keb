package dev.entao.keb.core

import dev.entao.kava.base.userName
import kotlin.reflect.KProperty

object ParamConst {
	const val BACK_URL = "backurl"
	const val ERROR = "errorMsg"
	const val SUCCESS = "successMsg"

	private const val _ERROR = "_error_"

	fun errField(pname: String): String {
		return _ERROR + pname
	}

	fun errField(p: KProperty<*>): String {
		return _ERROR + p.userName
	}

	fun isErrField(key: String): Boolean {
		return key.startsWith(_ERROR)
	}
}

fun UriMake.ok(msg: String): UriMake {
	return arg(ParamConst.SUCCESS, msg)
}

fun UriMake.err(msg: String): UriMake {
	return arg(ParamConst.ERROR, msg)
}

fun UriMake.success(msg: String): UriMake {
	return arg(ParamConst.SUCCESS, msg)
}

fun UriMake.error(msg: String): UriMake {
	return arg(ParamConst.ERROR, msg)
}

fun ReferUrl.ok(msg: String): ReferUrl {
	arg(ParamConst.SUCCESS, msg)
	return this
}

//上面显示的一个错误信息条
fun ReferUrl.err(msg: String): ReferUrl {
	arg(ParamConst.ERROR, msg)
	return this
}

//具体某个字段的错误信息
fun ReferUrl.errField(fieldName: String, msg: String): ReferUrl {
	arg(ParamConst.errField(fieldName), msg)
	return this
}

fun ReferUrl.withoutMessage(): ReferUrl {
	this.exclude(setOf(ParamConst.ERROR, ParamConst.SUCCESS))
	return this
}