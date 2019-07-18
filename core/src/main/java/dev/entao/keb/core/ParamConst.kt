package dev.entao.keb.core

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

fun WebPath.ok(msg: String): WebPath {
	return arg(ParamConst.SUCCESS, msg)
}

fun WebPath.err(msg: String): WebPath {
	return arg(ParamConst.ERROR, msg)
}

fun WebPath.success(msg: String): WebPath {
	return arg(ParamConst.SUCCESS, msg)
}

fun WebPath.error(msg: String): WebPath {
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
fun ReferUrl.errOf(name: String, msg: String): ReferUrl {
	arg(ParamConst.err(name), msg)
	return this
}