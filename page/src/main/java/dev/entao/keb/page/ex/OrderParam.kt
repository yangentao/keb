package dev.entao.keb.page.ex

import dev.entao.keb.page.P
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.userName
import dev.entao.kava.sql.SQLQuery

/**
 * Created by entaoyang@163.com on 2018/7/9.
 */

class OrderParam(val context: dev.entao.keb.core.HttpContext, p: Prop1, desc: Boolean = true) {

	val sortBy: String
	val desc: Boolean

	init {
		val ascKey = context.httpParams.str(P.ascKey)
		val descKey = context.httpParams.str(P.descKey)

		this.sortBy = ascKey ?: descKey ?: p.userName
		this.desc = if (ascKey == null && descKey == null) {
			desc
		} else {
			descKey != null
		}

	}
}

fun dev.entao.keb.core.HttpGroup.OrderBy(p: Prop1, desc: Boolean = true): OrderParam {
	return OrderParam(context, p, desc)
}

fun SQLQuery.orderBy(sp: OrderParam) {
	if (sp.sortBy.isNotEmpty()) {
		if (sp.desc) {
			desc(sp.sortBy)
		} else {
			asc(sp.sortBy)
		}
	}
}


