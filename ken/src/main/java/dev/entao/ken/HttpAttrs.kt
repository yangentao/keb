package dev.entao.ken

import dev.entao.kava.base.userName
import dev.entao.kava.base.MyDate
import java.sql.Date
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

class HttpAttrs(val context: HttpContext) {

	fun any(name: String): Any? {
		return context.request.attr(name)
	}

	fun any(p: KProperty<*>): Any? {
		return context.request.attr(p.userName)
	}

	fun any(p: KParameter): Any? {
		return context.request.attr(p.userName)
	}

	fun str(name: String): String? {
		return any(name) as? String
	}

	fun str(pa: KParameter): String? {
		return str(pa.userName)
	}

	fun str(prop: KProperty<*>): String? {
		return str(prop.userName)
	}

	fun trim(name: String): String? {
		return str(name)?.trim()
	}

	fun date(p: KProperty<*>): Date? {
		return date(p.userName)
	}

	fun date(name: String): Date? {
		val md = MyDate.parse(MyDate.FORMAT_DATE, trim(name) ?: "")
		if (md != null) {
			return Date(md.time)
		}
		return null
	}

	fun int(p: KProperty<*>): Int? {
		return any(p) as? Int
	}

	fun int(name: String): Int? {
		return int(name)
	}

	fun long(p: KProperty<*>): Long? {
		return any(p) as? Long
	}

	fun long(name: String): Long? {
		return any(name) as? Long
	}

}