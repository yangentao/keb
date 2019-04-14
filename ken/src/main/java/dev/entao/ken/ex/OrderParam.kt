package dev.entao.ken.ex

import dev.entao.kbase.Prop1
import dev.entao.kbase.userName
import dev.entao.ken.HttpContext
import dev.entao.ken.HttpPage
import dev.entao.kage.P
import dev.entao.sql.SQLQuery

/**
 * Created by entaoyang@163.com on 2018/7/9.
 */

class OrderParam(val context: HttpContext, p: Prop1, desc: Boolean = true) {

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

fun HttpPage.OrderBy(p: Prop1, desc: Boolean = true): OrderParam {
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


