package dev.entao.keb.page.widget

import dev.entao.kava.base.*
import dev.entao.kava.json.YsonObject
import dev.entao.kava.sql.ModelMap
import dev.entao.keb.core.*
import dev.entao.keb.page.OrderParam
import dev.entao.keb.page.P
import dev.entao.keb.page.bootstrap.a
import dev.entao.keb.page.bootstrap.buttonApplyTheme
import dev.entao.keb.page.bootstrap.buttonX
import dev.entao.keb.page.bootstrap.linkButtonX
import dev.entao.keb.page.displayOf
import dev.entao.keb.page.modules.Upload
import dev.entao.keb.page.tag.*
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1

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
		it += _table_striped
	}
	var onHeadStyle: (Tag) -> Unit = {
		it += _thead_light
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
			this += _table
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
							th(scope_ to "col") {
								ci.onHeader(this)
							}
						}
					}
					val headId = needId()
					script {
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
		if (it.tagName == "button") {
			it += "m-1"
		}
	}
}

class TableActionPanel(parentTag: Tag) {
	val tag = parentTag.div(class_ to _d_flex.._flex_row.._justify_content_start.."m-1", block = {})


	fun deleteChecked(action: HttpAction): Tag {
		val b = this.actionChecked(action)
		b[class_] = _btn.._btn_danger
		if (b[data_confirm_].isEmpty()) {
			b[data_confirm_] = "要删除选中记录吗?"
		}
		return b
	}

	fun actionChecked(action: HttpAction): Tag {
		return tag.buttonX(action) {
			this.buttonApplyTheme(action, _btn_outline_primary.value)
			val btnId = needId()
			script {
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

	fun dialogChecked(action: KFunction<*>): Tag {
		return tag.buttonX(action) {
			this[onclick_] = "Yet.openDialogPanelChecked(this);"
		}

	}

	fun action(action: HttpAction): Tag {
		return tag.buttonX(action) {
			val btnId = needId()
			script {
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

	fun dialog(action: KFunction<Unit>): Tag {
		return tag.buttonX(action) {
			this[onclick_] = "Yet.openDialogPanel(this);"
		}

	}
}


fun emWidthOfString(text: String): Double {
	var n: Double = 0.0
	for (ch in text) {
		val a = ch.toInt()
		if (a in 1..254) {
			n += 0.4
		} else {
			n += 0.8
		}
	}
	return n
}

abstract class XColumn<T>(val table: XTable<T>) {
	val httpContext: HttpContext
		get() {
			return table.tableTag.httpContext
		}

	open fun onFinished() {}
	open fun onHeaderValue() {}

	open fun onCellValue(item: T) {}

	abstract fun onCol(colTag: Tag)

	abstract fun onHeader(headTag: Tag)

	abstract fun onCell(tdTag: Tag, item: T)
}

open class XTextColumn<T>(table: XTable<T>) : XColumn<T>(table) {
	private val columnValues: ArrayList<String> = ArrayList(64)
	private var current: Int = 0
	private var sortName: String = ""
	private var titleValue: String = ""

	private var linkAction: HttpAction? = null
	private var linkProp1: Prop1? = null
	private var renderButton = false

	var linkTarget: String = ""

	var onCellTextValue: (T) -> String = { "$it" }

	var onCellRenderText: (Tag, String, T) -> Unit = { tag, v, _ ->
		tag.textEscaped(v)
	}

	fun title(title: String) {
		this.titleValue = title
	}

	fun sortBy(p: Prop) {
		this.sortBy(p.userName)
	}

	fun sortBy(name: String) {
		if (table.sortCol.isNotEmpty()) {
			this.sortName = name
		}
	}

	fun linkTo(action: HttpAction, p1: Prop1?) {
		linkAction = action
		this.linkProp1 = p1
	}

	fun buttonTo(action: HttpAction, p1: Prop1?) {
		linkAction = action
		this.linkProp1 = p1
		renderButton = true
		sortName = ""
	}

	override fun onCellValue(item: T) {
		columnValues += this.onCellTextValue(item)
	}

	override fun onCol(colTag: Tag) {
		val n1: Double = this.columnValues.map { emWidthOfString(it) }.max() ?: 0.0
		val n2: Double = emWidthOfString(this.titleValue)
		var n: Double = Math.max(n1, n2)
		if (n < 1) {
			n = 1.0
		}
		if (n < 20) {
			colTag[style_] = "width:${n}em"
		} else {
			colTag[style_] = "width:*"
		}
	}

	override fun onHeader(headTag: Tag) {
		if (this.titleValue.isEmpty()) {
			return
		}
		if (sortName.isEmpty()) {
			headTag.textEscaped(titleValue)
			return
		}
		headTag.a(role_ to "button", href_ to "#", P.dataSortCol to sortName) {
			this.textEscaped(this@XTextColumn.titleValue)
			if (table.sortCol == sortName) {
				if (table.desc) {
					this[P.dataDesc] = "false"
					textUnsafe(V.DownArrow)
				} else {
					this[P.dataDesc] = "true"
					textUnsafe(V.UpArrow)
				}
			}
		}
	}

	override fun onCell(tdTag: Tag, item: T) {
		val v = this.columnValues[this.current]
		if (v.length > 16) {
			tdTag[style_] = "white-space: normal;"
		}
		this.current += 1
		val ac = this.linkAction
		if (ac == null) {
			this.onCellRenderText(tdTag, v, item)
			return
		}
		val v2: String = linkProp1?.valOf(item as Any) ?: v

		if (renderButton) {
			tdTag.linkButtonX(ac + v2).apply {
				if (linkTarget.isNotEmpty()) {
					this[target_] = linkTarget
				}
			}
		} else {
			tdTag.a( ac + v2, v ).apply {
				if (linkTarget.isNotEmpty()) {
					this[target_] = linkTarget
				}
			}
		}
	}
}

class XPropColumn<T>(table: XTable<T>, val prop: Prop1) : XTextColumn<T>(table) {
	init {
		if (prop is KMutableProperty1) {
			this.sortBy(prop.userName)
		}
		this.title(prop.userLabel)
		this.onCellTextValue = {
			val v: Any? = when (it) {
				is YsonObject -> it.getValue(it, prop)
				is ModelMap -> it[prop]
				is Map<*, *> -> it.get(prop.userName)
				else -> prop.getValue(it as Any)

			}
			if (v == null) {
				""
			} else {
				displayOf(prop, v)
			}
		}
	}

	fun linkTo(action: KFunction<Unit>): XPropColumn<T> {
		super.linkTo(action, prop)
		return this
	}
}

class XCheckColumn<T>(table: XTable<T>, val prop: Prop1) : XColumn<T>(table) {

	override fun onCol(colTag: Tag) {
		colTag[style_] = "width:2em"
	}

	override fun onHeader(headTag: Tag) {
		headTag.checkbox {
			id = "checkall"
			script {
				"""
				$('#checkall').click(function (e) {
					$(this).closest('table').find('tbody td input:checkbox').prop('checked', this.checked);
				});

				"""
			}
		}
	}

	override fun onCell(tdTag: Tag, item: T) {
		tdTag.checkbox(value_ to prop.valOf(item as Any)) {
		}
	}

}

class XColumnActionGroup<T>(table: XTable<T>) : XColumn<T>(table) {
	private var colTag: Tag? = null
	private var labelList: List<String> = emptyList()

	var callback: XColumnAction<T>.() -> Unit = {}

	override fun onCol(colTag: Tag) {
		this.colTag = colTag
	}

	override fun onHeader(headTag: Tag) {
		headTag.textEscaped("操作")
		headTag += "pl-3"
	}

	override fun onCell(tdTag: Tag, item: T) {
		val a = XColumnAction(tdTag, item)
		a.callback()
		this.labelList = a.labelList
	}

	override fun onFinished() {
		val a = labelList.map { emWidthOfString(it) + 0.5 }.sum()
		val b = emWidthOfString("操作")
		val c = Math.max(a, b).keepDot(1)
		this.colTag?.set(style_, "width:${c}em")
	}

}

class XColumnAction<T>(val td: Tag, val item: T) {

	val labelList = ArrayList<String>(4)
	val group = td.flexRow {
		this += _justify_content_start
	}

	fun actionLinkProp(action: HttpAction, prop: Prop1, block: Tag.() -> Unit = {}): Tag {
		val v = prop.getValue(item as Any)
		val lk = group.linkButtonX(action + (v?.toString() ?: ""))
		lk[class_] = "px-2"
		lk.block()
		labelList += action.userLabel
		return lk
	}

	fun actionLink(action: ActionURL): Tag {
		val t = group.linkButtonX(action)
		t[class_] = "px-2"
		labelList += action.action.userLabel
		return t
	}
}