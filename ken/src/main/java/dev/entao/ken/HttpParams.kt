package dev.entao.ken

import dev.entao.kbase.userName
import dev.entao.kbase.MyDate
import java.sql.Date
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

class HttpParams(val context: HttpContext) {

	operator fun get(property: KProperty<*>): String {
		return str(property.userName) ?: ""
	}

	operator fun get(key: String): String {
		return str(key) ?: ""
	}

	fun str(name: String): String? {
		return context.request.param(name)
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
		return str(p)?.trim()?.toIntOrNull()
	}

	fun int(name: String): Int? {
		return str(name)?.trim()?.toIntOrNull()
	}

	fun long(p: KProperty<*>): Long? {
		return str(p)?.trim()?.toLongOrNull()
	}

	fun long(name: String): Long? {
		return str(name)?.trim()?.toLongOrNull()
	}

	fun double(p: KProperty<*>): Double? {
		return str(p)?.trim()?.toDoubleOrNull()
	}

	fun double(name: String): Double? {
		return str(name)?.trim()?.toDoubleOrNull()
	}

	fun bool(p: KProperty<*>): Boolean? {
		return bool(p.userName)
	}

	fun bool(name: String): Boolean? {
		val s = trim(name) ?: return null
		return s == "on" || s == "1" || s == "true"
	}

}