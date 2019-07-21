package dev.entao.keb.page.widget

import dev.entao.keb.page.ex.Upload
import dev.entao.kava.base.Prop1
import dev.entao.kava.base.getValue
import dev.entao.keb.core.HttpAction
import dev.entao.keb.page.*
import dev.entao.keb.page.html.*
import dev.entao.keb.page.ex.OrderParam
import java.util.*
import kotlin.reflect.KFunction

fun <T> Tag.tableX(items: List<T>, sp: OrderParam? = null, block: XTable<T>.() -> Unit) {
	val b = XTable(this, items)
	b.orderByParam(sp)
	b.block()
	b.build()
}

class XTable<T>(tableParent: Tag, val items: List<T>) {

	val tableTag = tableParent.table { }

	var sortCol: String = ""
	var desc = false

	val columnList = ArrayList<XColumn<T>>()

	var onTr: (Tag, T) -> Unit = { _, _ ->

	}
	var onTableStyle: (Tag) -> Unit = {
		it.addClass(B.tableStriped)
	}
	var onHeadStyle: (Tag) -> Unit = {
		it.addClass(B.theadLight)
	}

	private var withHeader = true

	fun headerLess() {
		withHeader = false
	}

	fun orderByParam(sp: OrderParam?) {
		if (sp != null) {
			this.sortCol = sp.sortBy
			this.desc = sp.desc
		}
	}

	fun column(col: XColumn<T>) {
		columnList += col
	}

	fun column(title: String, block: (T) -> String): XTextColumn<T> {
		val c = XTextColumn(this)
		c.title(title)
		c.onCellTextValue = block
		this.column(c)
		return c
	}

	fun column(prop: Prop1): XPropColumn<T> {
		val c = XPropColumn(this, prop)
		this.column(c)
		return c
	}

	fun columnCheck(prop: Prop1): XCheckColumn<T> {
		val c = XCheckColumn(this, prop)
		this.column(c)
		return c
	}

	fun columnRes(p: Prop1, downAction: HttpAction) {
		this.column(p).apply {
			this.onCellTextValue = { item ->
				val v: Int? = p.getValue(item as Any) as? Int
				if (v == null || v == 0) {
					""
				} else {
					Upload.findByKey(v)?.rawname ?: ""
				}
			}
			this.linkTo(downAction, p)
		}
	}

	fun columnActionGroup(block: XColumnAction<T>.() -> Unit) {
		val c = XColumnActionGroup<T>(this)
		c.callback = block
		column(c)
	}

	fun build() {
		tableTag.needId()
		for (c in columnList) {
			c.onHeaderValue()
		}
		for (item in items) {
			for (c in columnList) {
				c.onCellValue(item)
			}
		}

		tableTag.apply {
			addClass(B.table)
			this@XTable.onTableStyle(this)
//			colgroup {
//				columnList.forEach { colInfo ->
//					col {
//						colInfo.onCol(this)
//					}
//				}
//			}
			if (withHeader) {
				thead {
					this@XTable.onHeadStyle(this)
					tr {
						columnList.forEach { ci ->
							th {
								scope = "col"
								ci.onHeader(this)
							}
						}
					}
					val headId = needId()
					scriptBlock {
						"""
						Yet.sortCol = '$sortCol';
						Yet.desc = $desc;
						$('#$headId').find('a').click(function(e){
							Yet.sortCol = $(this).attr('${P.dataSortCol}');
							Yet.desc = ${'$'}(this).attr('${P.dataDesc}') === "true";
							Yet.listFilter();
							return false ;
						});
						""".trimIndent()
					}

				}
			}
			tbody {
				items.forEach { item ->
					tr {
						onTr(this, item)
						columnList.forEach { ci ->
							this.td {
								ci.onCell(this, item)
							}
						}
					}
				}
			}
			for (c in columnList) {
				c.onFinished()
			}
		}
	}
}

fun Tag.tableActionPanel(block: TableActionPanel.() -> Unit) {
	val b = TableActionPanel(this)
	b.block()
	b.tag.children.forEach {
		if (it is ButtonTag) {
			it.classList += "m-1"
		}
	}
}

class TableActionPanel(parentTag: Tag) {
	val tag = parentTag.addTag(Tag(parentTag.httpContext, "div"))

	init {
		tag.addClass(B.flex, B.Flex.row, B.Flex.justifyContentStart, "m-1")
	}

	fun deleteChecked(action: HttpAction): ButtonTag {
		val b = this.actionChecked(action)
		b.btnDanger()
		if (b.dataConfirm.isEmpty()) {
			b.dataConfirm = "要删除选中记录吗?"
		}
		return b
	}

	fun actionChecked(action: HttpAction): ButtonTag {
		return tag.button {
			this.fromAction(action)
			val btnId = needId()
			scriptBlock {
				"""
				$('#$btnId').click(function(e){
					var s = Yet.findCheckedIds();
					if(s.length>0){
						var cs = $(this).attr("data-confirm");
						var url = $(this).attr("data-url") ;
						var argName = $(this).attr("data-param-name");
						if(!cs || confirm(cs)){
							Yet.uncheckAll();
							Yet.reloadGet(url, argName + '=' + s);
						}
					}
					return false;
				});
				"""
			}
		}
	}

	fun dialogChecked(action: KFunction<Unit>): ButtonTag {
		return tag.button {
			fromAction(action)
			onclick = "Yet.openDialogPanelChecked(this);"
		}

	}

	fun action(action: HttpAction): ButtonTag {
		return tag.button {
			this.fromAction(action)
			val btnId = needId()
			scriptBlock {
				"""
				$('#$btnId').click(function(e){
						var cs = $(this).attr("data-confirm");
						var url = $(this).attr("data-url") ;
						if(!cs || confirm(cs)){
							Yet.reloadGet(url, null);
						}
						return false;
				});
				"""
			}
		}
	}

	fun dialog(action: KFunction<Unit>): ButtonTag {
		return tag.button {
			fromAction(action)
			onclick = "Yet.openDialogPanel(this);"
		}

	}
}


