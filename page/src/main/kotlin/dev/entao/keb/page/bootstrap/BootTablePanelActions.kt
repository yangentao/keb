package dev.entao.keb.page.bootstrap

import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.HttpContext
import dev.entao.keb.page.tag.*
import kotlin.reflect.KFunction


fun Tag.tableActionPanel(forTableId: String, block: TableActionPanel.() -> Unit) {
	val b = TableActionPanel(this.httpContext, forTableId)
	add(b)
	b.block()
	b.children.forEach {
		if (it.tagName == "button") {
			it += "m-1"
			it += _btn_sm
		}
	}
}

class TableActionPanel(context: HttpContext, val forTableId: String) : Tag(context, "div") {
	init {
		this[class_] = _d_flex.._flex_row.._justify_content_start.._my_1
	}


	fun deleteChecked(action: HttpAction): Tag {
		val b = this.actionChecked(action)
		b[class_] = _btn.._btn_danger
		if (b[data_confirm_].isEmpty()) {
			b[data_confirm_] = "要删除选中记录吗?"
		}
		return b
	}

	fun actionChecked(action: HttpAction): Tag {
		return buttonX(action) {
			buttonApplyTheme(action, _btn_outline_primary.value)
			this[onclick_] = "tableCheckReload(this,'$forTableId');"
		}
	}

	fun dialogChecked(action: KFunction<*>): Tag {
		return buttonX(action) {
			buttonApplyTheme(action, _btn_outline_primary.value)
			this[onclick_] = "tableCheckOpenDialog(this, '$forTableId');"
		}

	}


}

