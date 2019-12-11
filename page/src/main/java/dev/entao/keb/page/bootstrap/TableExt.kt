package dev.entao.keb.page.bootstrap

import dev.entao.kava.base.*
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.plus
import dev.entao.keb.page.ActionDanger
import dev.entao.keb.page.tag.*


abstract class ColumnBuilder<T> {
	var textLimit = 100
	abstract fun onTh(tag: Tag)

	abstract fun onTd(tag: Tag, item: T)
}

open class ActionColumn<T>(val prop: Prop1, vararg val actions: HttpAction) : ColumnBuilder<T>() {
	var label: String = "操作"

	fun label(s: String): ActionColumn<T> {
		this.label = s
		return this
	}

	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		val argV = prop.getValue(item as Any)?.toString() ?: ""
		tag.span {
			for (ac in actions) {
				if (ac.hasAnnotation<ActionDanger>()) {
					linkButtonX(ac + argV, class_ to _btn_outline_danger.._btn_sm.._mr_2)
				} else {
					linkButtonX(ac + argV, class_ to _btn_outline_primary.._btn_sm.._mr_2)
				}

			}
		}
	}
}

open class LinkColumn<T>(val prop: Prop1, val linkTo: HttpAction, val argProp: Prop1 = prop, val label: String = prop.userLabel) : ColumnBuilder<T>() {
	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		val displayValue = prop.getValue(item as Any)?.toString() ?: ""
		val argV: String = if (prop === argProp) {
			displayValue
		} else {
			argProp.getValue(item as Any)?.toString() ?: ""
		}
		tag.a(linkTo + argV, displayValue.head(textLimit))
	}
}


open class PropColumn<T>(val prop: Prop1, val label: String = prop.userLabel) : ColumnBuilder<T>() {
	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		tag.textEscaped(prop.getValue(item as Any)?.toString()?.head(textLimit))
	}
}

open class KeyColumn<T>(val key: String, val label: String = key) : ColumnBuilder<T>() {
	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		if (item is Map<*, *>) {
			tag.textEscaped(item[key]?.toString()?.head(textLimit))
		}
	}
}

open class IndexColumn<T>(val index: Int, val label: String) : ColumnBuilder<T>() {
	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		if (item is List<*>) {
			tag.textEscaped(item[index]?.toString()?.head(textLimit))
		} else if (item is Array<*>) {
			tag.textEscaped(item[index]?.toString()?.head(textLimit))
		}
	}
}

fun <T : Any> Tag.tableT(items: List<T>, cbList: List<ColumnBuilder<T>>, callback: TagCallback): Tag {
	return tableResponsive {
		tableNormal {
			thead {
				tr {
					for (cb in cbList) {
						th(scope_ to "col") {
							cb.onTh(this)
						}
					}
				}
			}
			tbody {
				for (item in items) {
					tr {
						for (cb in cbList) {
							td {
								cb.onTd(this, item)
							}
						}
					}
				}
			}
			this.callback()
		}
	}
}
