package dev.entao.keb.page

import dev.entao.kava.base.*
import dev.entao.kava.json.YsonValue
import dev.entao.keb.core.Keb
import dev.entao.keb.page.tag.Tag
import dev.entao.keb.page.bootstrap.alertError
import dev.entao.keb.page.bootstrap.alertSuccess
import dev.entao.keb.page.widget.singleSelectDisplay
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/4/8.
 */

class LinkItem(val label: String, val url: String, var active: Boolean = false) {
	val children: ArrayList<LinkItem> = ArrayList()
}

fun Tag.showMessagesIfPresent() {
	val s = this.httpContext.httpParams.str(Keb.ERROR) ?: ""
	if (s.isNotEmpty()) {
		this.alertError(s)
	}
	val ss = this.httpContext.httpParams.str(Keb.SUCCESS) ?: ""
	if (ss.isNotEmpty()) {
		this.alertSuccess(ss)
	}
}

private val IDENT = "    "
val QUOT = "\""

fun Appendable.ident(n: Int) {
	if (n > 0) {
		for (i in 0 until n) {
			this.append(IDENT)
		}
	}
}

fun attrVal(value: String): String {
	val s = value.replace(QUOT, "&quot;")
	return QUOT + s + QUOT
}

private var idInc: Int = 0

@Synchronized
fun eleId(prefix: String = ""): String {
	++idInc
	return prefix + "_$idInc"
}

fun displayOf(p0: Prop0): String {
	return displayOf(p0, p0.getValue())
}

fun displayOf(p: Prop, v: Any?): String {
	if (v == null) {
		return ""
	}
	if (v is YsonValue) {
		return v.yson()
	}

	val kd = p.findAnnotation<KeepDot>()
	if (kd != null) {
		if (v is Double) {
			return v.keepDot(kd.value)
		}
		if (v is Float) {
			return v.toDouble().keepDot(kd.value)
		}
	}

	val a = p.singleSelectDisplay(v)
	if (a != null) {
		return a
	}
	val fd = p.findAnnotation<FormDate>()
	if (fd != null) {
		if (v is Long) {
			if (v == 0L) {
				return ""
			} else {
				return MyDate(v).format(fd.value)
			}
		} else if (v is java.util.Date) {
			return MyDate(v.time).format(fd.value)
		}
	}
	val ft = p.findAnnotation<FormTime>()
	if (ft != null) {
		if (v is Long) {
			return MyDate(v).format(ft.value)
		} else if (v is java.util.Date) {
			return MyDate(v.time).format(ft.value)
		}
	}
	return v.toString()
}