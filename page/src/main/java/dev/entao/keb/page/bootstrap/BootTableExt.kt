package dev.entao.keb.page.bootstrap

import dev.entao.kava.base.*
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.plus
import dev.entao.keb.core.valOf
import dev.entao.keb.page.ActionDanger
import dev.entao.keb.page.SortParam
import dev.entao.keb.page.P
import dev.entao.keb.page.displayOf
import dev.entao.keb.page.modules.Upload
import dev.entao.keb.page.tag.*


abstract class ColumnBuilder<T> {
	var sortName: String = ""
	var textLimit = 100

	fun sortNamed(sortName: String): ColumnBuilder<T> {
		this.sortName = sortName
		return this
	}

	abstract fun onTh(tag: Tag)

	abstract fun onTd(tag: Tag, item: T)
}

open class CheckColumn<T>(val prop: Prop1) : ColumnBuilder<T>() {
	override fun onTh(tag: Tag) {
		tag["width"] = "4em"
		tag.checkbox {
			val cid = needId()
			script {
				"""
				$('#$cid').click(function (e) {
					$(this).closest('table').find('tbody td input:checkbox').prop('checked', this.checked);
				});

				"""
			}
		}
	}

	override fun onTd(tag: Tag, item: T) {
		tag.checkbox(value_ to prop.valOf(item as Any)) {
		}
	}
}

open class ActionColumn<T>(val prop: Prop1, vararg val actions: HttpAction) : ColumnBuilder<T>() {
	var label: String = "操作"

	fun label(s: String): ActionColumn<T> {
		this.label = s
		return this
	}

	override fun onTh(tag: Tag) {
		tag += _text_right.._pr_3
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		tag += _text_right
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

	fun sortable(): LinkColumn<T> {
		this.sortNamed(prop.userName)
		return this
	}

	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		val displayValue = displayOf(prop, item)
		val argV: String = if (prop === argProp) {
			displayValue
		} else {
			argProp.getValue(item as Any)?.toString() ?: ""
		}
		tag.a(linkTo + argV, displayValue.head(textLimit))
	}
}

open class ResColumn<T>(val prop: Prop1, val downAction: HttpAction, val label: String = prop.userLabel) : ColumnBuilder<T>() {


	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		val v: Int? = prop.getValue(item as Any) as? Int
		val displayValue = if (v == null || v == 0) {
			""
		} else {
			Upload.findByKey(v)?.rawname ?: ""
		}
		if (v != null) {
			tag.a(downAction + v, displayValue.head(textLimit))
		}
	}
}

open class PropColumn<T>(val prop: Prop1, val label: String = prop.userLabel) : ColumnBuilder<T>() {

	fun sortable(): PropColumn<T> {
		this.sortNamed(prop.userName)
		return this
	}

	override fun onTh(tag: Tag) {
		tag.textEscaped(this.label)
	}

	override fun onTd(tag: Tag, item: T) {
		val disp = displayOf(prop, item)
		tag.textEscaped(disp.head(textLimit))
	}
}

open class KeyColumn<T>(val key: String, val label: String = key) : ColumnBuilder<T>() {

	fun sortable(): KeyColumn<T> {
		this.sortNamed(key)
		return this
	}

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

fun <T : Any> Tag.tableT(items: List<T>, cbList: List<ColumnBuilder<T>>, sortParam: SortParam, callback: TagCallback): Tag {
	return tableT(items, cbList, sortParam.sortBy, sortParam.desc, callback)
}

fun <T : Any> Tag.tableT(items: List<T>, cbList: List<ColumnBuilder<T>>, sortName: String, desc: Boolean, callback: TagCallback): Tag {
	return tableResponsive {
		tableNormal {
			this += P.dataSortBy to sortName
			this += P.dataSortDesc to if (desc) "1" else "0"
			thead {
				tr {
					for (cb in cbList) {
						th(scope_ to "col") {
							cb.onTh(this)
							if (cb.sortName.isNotEmpty()) {
								a(role_ to "button", href_ to "#", P.dataSortBy to cb.sortName) {
									var sortIcon = "fa-sort"
									if (sortName == cb.sortName) {
										sortIcon = if (desc) {
											"fa-sort-down"
										} else {
											"fa-sort-up"
										}
									}
									i(class_ to "fas $sortIcon ml-1")
								}
							}
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
			val tid = this.needId()
			script {
				"""
					$(function(){
						$('#$tid').find("thead").find('a').click(function(e){
							e.preventDefault();
							let sortColName = $(this).attr('${P.dataSortBy}');
							let desc = $('#$tid').attr('${P.dataSortDesc}');
							let oldSortCol = $('#$tid').attr('${P.dataSortBy}');
							var newDesc = "1";
							if(oldSortCol.length == 0 || oldSortCol != sortColName){
								newDesc = "0";
							}else {
								if(desc == '1'){
									newDesc = "0";
								}else {
									newDesc = "1";
								}
							}
							searchBy2('${P.sortBy}', sortColName, '${P.sortDesc}', newDesc);
							return false;
						});
					});
				""".trimIndent()
			}
		}
	}
}
