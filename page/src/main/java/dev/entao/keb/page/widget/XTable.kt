package dev.entao.keb.page.widget

import dev.entao.kava.base.*
import dev.entao.kava.json.YsonObject
import dev.entao.kava.sql.ModelMap
import dev.entao.keb.page.modules.Upload
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.UriMake
import dev.entao.keb.core.valOf
import dev.entao.keb.page.*
import dev.entao.keb.page.html.*
import dev.entao.keb.page.ex.OrderParam
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
	val tag = parentTag.tag(Tag(parentTag.httpContext, "div"))

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
			colTag.style = "width:${n}em"
		} else {
			colTag.style = "width:*"
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
		headTag.a {
			role = B.button
			href = "#"
			attr(P.dataSortCol, sortName)
			this.textEscaped(this@XTextColumn.titleValue)
			if (table.sortCol == sortName) {
				if (table.desc) {
					attr(P.dataDesc, "false")
					textUnsafe(B.DownArrow)
				} else {
					attr(P.dataDesc, "true")
					textUnsafe(B.UpArrow)
				}
			}
		}
	}

	override fun onCell(tdTag: Tag, item: T) {
		val v = this.columnValues[this.current]
		if (v.length > 16) {
			tdTag.style = "white-space: normal;"
		}
		this.current += 1
		val ac = this.linkAction
		if (ac == null) {
			this.onCellRenderText(tdTag, v, item)
			return
		}
		val v2: String = linkProp1?.valOf(item as Any) ?: v

		if (renderButton) {
			tdTag.linkButton(ac) { param(v2) }.btnSmall().apply {
				if (linkTarget.isNotEmpty()) {
					this.target = linkTarget
				}
			}
		} else {
			val p = httpContext.actionUri(ac, v2)
			tdTag.a(v, p).apply {
				if (linkTarget.isNotEmpty()) {
					this.target = linkTarget
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
		colTag.style = "width:2em"
	}

	override fun onHeader(headTag: Tag) {
		headTag.checkbox {
			id = "checkall"
			scriptBlock {
				"""
				$('#checkall').click(function (e) {
					$(this).closest('table').find('tbody td input:checkbox').prop('checked', this.checked);
				});

				"""
			}
		}
	}

	override fun onCell(tdTag: Tag, item: T) {
		tdTag.checkbox {
			value = prop.valOf(item as Any)
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
		headTag.classList += "pl-3"
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
		this.colTag?.style = "width:${c}em"
	}

}

class XColumnAction<T>(val td: Tag, val item: T) {

	val labelList = ArrayList<String>(4)
	val group = td.flexRow {
		classList += B.Flex.justifyContentStart
	}

	fun actionLinkProp(action: HttpAction, prop: Prop1, block: LinkButton.() -> Unit = {}): LinkButton {
		val v = prop.getValue(item as Any)
		val lk = group.linkButton(action) { param(v) }
		lk.classList.clear()
		lk.addClass("px-2")
		lk.block()
		labelList += action.userLabel
		return lk
	}

	fun actionLink(action: HttpAction, block: UriMake.() -> Unit): LinkButton {
		val t = group.linkButton(action, block)
		t.classList.clear()
		t.addClass("px-2")
		labelList += action.userLabel
		return t
	}
}