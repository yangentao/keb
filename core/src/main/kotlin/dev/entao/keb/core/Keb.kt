package dev.entao.keb.core

import dev.entao.kava.base.userName
import kotlin.reflect.KProperty

object Keb {
	const val BACK_URL = "backurl"
	const val ACCOUNT = "account"
	const val ACCOUNT_NAME = "account_name"

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

fun UriMake.success(msg: String): UriMake {
	return arg(Keb.SUCCESS, msg)
}

fun UriMake.error(msg: String): UriMake {
	return arg(Keb.ERROR, msg)
}

fun ReferUrl.success(msg: String): ReferUrl {
	arg(Keb.SUCCESS, msg)
	return this
}

//上面显示的一个错误信息条
fun ReferUrl.error(msg: String): ReferUrl {
	arg(Keb.ERROR, msg)
	return this
}

//具体某个字段的错误信息
fun ReferUrl.errField(fieldName: String, msg: String): ReferUrl {
	arg(Keb.errField(fieldName), msg)
	return this
}

fun ReferUrl.withoutMessage(): ReferUrl {
	this.exclude(setOf(Keb.ERROR, Keb.SUCCESS))
	return this
}