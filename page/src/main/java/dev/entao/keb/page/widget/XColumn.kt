package dev.entao.keb.page.widget

import dev.entao.keb.page.*
import dev.entao.kava.base.*
import dev.entao.kava.json.YsonObject
import dev.entao.kava.sql.ModelMap
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.UriMake
import dev.entao.keb.core.valOf
import dev.entao.keb.page.B
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.html.scriptBlock
import dev.entao.keb.page.html.targetBlank
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1

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
	val httpContext: dev.entao.keb.core.HttpContext
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

	fun linkTo(action: KFunction<Unit>, p1: Prop1?) {
		linkAction = action
		this.linkProp1 = p1
	}

	fun buttonTo(action: KFunction<Unit>, p1: Prop1?) {
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
				targetBlank()
			}
		} else {
			val p = httpContext.actionUri(ac, v2)
			tdTag.a(v, p).apply {
				targetBlank()
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

	fun linkTo(action: KFunction<Unit>) {
		super.linkTo(action, prop)
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