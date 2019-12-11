package dev.entao.keb.core

object Keb {
	const val BACK_URL = "backurl"
	const val ACCOUNT_ID = "accountId"
	const val ACCOUNT = "account"
	const val ACCOUNT_NAME = "account_name"

	const val ERROR = "errorMsg"
	const val SUCCESS = "successMsg"

	fun errField(name: String): String {
		return name + "__error__"
	}

}

