package dev.entao.kava.sql.ext

import dev.entao.kava.base.*
import dev.entao.kava.sql.*
import java.lang.Integer.max
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

//限制表的记录行数, 用户Log表, IP表等需要定时删除的表
//必须定义Int或Long类型的自增主键, 并使用@PrimaryKey, @AutoInc修饰主键
//TableLimitTimer(Ip::class, 10000)  限制Ip表10000行记录, 每小时删除一次旧数据
//TableLimitTimer(Ip::class)  函数有MaxRows注释决定
class TableLimit(private val cls: KClass<out Model>, limitValue: Int = 0) {

	private val maxRow: Int = max(limitValue, cls.findAnnotation<LimitRows>()?.value ?: 100)
	private val pk: Prop

	init {
		val ks = cls.modelPrimaryKeys
		assert(ks.size == 1)
		pk = ks.first()
		assert(pk.hasAnnotation<AutoInc>())
		assert(pk.isTypeLong || pk.isTypeInt)
	}


	fun limit() {
		limitTable(cls, pk, maxRow)
	}


	companion object {
		fun limitTable(cls: KClass<out Model>, pk: Prop, maxRow: Int) {
			if (maxRow <= 0) {
				return
			}
			val c = cls.namedConn
			val r = c.query {
				select(pk)
				from(cls)
				desc(pk)
				limit(1, maxRow)
			}

			if (pk.isTypeInt) {
				val n = r.firstRow { it.getInt(1) } ?: return
				c.delete(cls, pk LT n)
			} else if (pk.isTypeLong) {
				val n = r.firstRow { it.getLong(1) } ?: return
				c.delete(cls, pk LT n)
			} else {
				r.closeSafe()
			}

		}
	}

}