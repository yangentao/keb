package yet.servlet

import dev.entao.kbase.*
import dev.entao.sql.AutoInc
import dev.entao.ken.anno.MaxRows
import dev.entao.sql.PrimaryKey
import dev.entao.yog.loge
import dev.entao.sql.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

interface HttpTimer {
	fun onHour(h: Int){}
	fun onMinute(m: Int){}
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

		if (pk.isTypeInt) {
			val n = r.intValue ?: return
			c.delete(cls, pk LT n)
		} else if (pk.isTypeLong) {
			val n = r.longValue ?: return
			c.delete(cls, pk LT n)
		} else {
			r.closeSafe()
		}

	}

}