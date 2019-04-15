@file:Suppress("unused")

package dev.entao.keb.core

import dev.entao.kava.base.*
import dev.entao.kava.log.loge
import dev.entao.kava.sql.*
import dev.entao.keb.core.anno.MaxRows
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

interface Acceptor {
	fun accept(context: HttpContext, router: Router): Boolean
}

interface PermAcceptor {
	fun prepare(context: HttpContext)
	fun accept(context: HttpContext, uri: String): Boolean
}

interface HttpTimer {
	fun onHour(h: Int) {}
	fun onMinute(m: Int) {}
}


//============

object MethodAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		if (router.methods.isNotEmpty()) {
			if (context.request.method.toUpperCase() !in router.methods) {
				context.abort(400, "Method Error")
				return false
			}
		}
		return true
	}

}

//限制表的记录行数
//TableLimitTimer(Ip::class, 10000)  限制Ip表10000行记录, 每小时删除一次旧数据
//TableLimitTimer(Ip::class)  函数有MaxRows注释决定
class TableLimitTimer(private val cls: KClass<out Model>, limitValue: Int = 0) : HttpTimer {

	private val maxRow: Int = if (limitValue > 0) {
		limitValue
	} else {
		cls.findAnnotation<MaxRows>()?.value ?: 0
	}
	private val pk: Prop1?

	init {
		pk = prepare()
	}

	private fun prepare(): Prop1? {
		if (maxRow <= 0) {
			return null
		}
		val ks = cls.modelPrimaryKeys
		if (ks.size != 1) {
			loge("必须是唯一整形自增主键")
			return null
		}
		val pk = ks.first()
		if (!(pk.hasAnnotation<PrimaryKey>() && pk.hasAnnotation<AutoInc>())
				|| !(pk.isTypeLong || pk.isTypeInt)) {
			loge("必须是整形自增主键")
			return null
		}
		return pk as Prop1

	}

	override fun onHour(h: Int) {
		if (pk != null) {
			this.limitTable(cls, pk, maxRow)
		}
	}

	private fun limitTable(cls: KClass<out Model>, pk: Prop1, maxRow: Int) {
		if (maxRow <= 0) {
			return
		}
		val c = ConnLook.named(cls)
		val r = c.query {
			select(pk)
			from(cls)
			desc(pk)
			limit(1, maxRow)
		}

		when {
			pk.isTypeInt -> {
				val n = r.intValue ?: return
				c.delete(cls, pk LT n)
			}
			pk.isTypeLong -> {
				val n = r.longValue ?: return
				c.delete(cls, pk LT n)
			}
			else -> r.closeSafe()
		}

	}

}



