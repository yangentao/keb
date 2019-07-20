@file:Suppress("unused")

package dev.entao.keb.page

import dev.entao.kava.sql.SQLQuery
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.html.HtmlDoc
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.html.bodyTag
import dev.entao.keb.page.html.p
import dev.entao.keb.page.widget.button

fun SQLQuery.limitPage(context: HttpContext) {
	val n = context.httpParams.int(P.pageN) ?: 0
	this.limit(P.pageSize, n * P.pageSize)
}

fun HttpScope.formError(title: String, msg: String) {
	val d = DialogBuild(context)
	d.modal.outputScript = true
	d.title(title)
	d.bodyBlock = {
		it.p {
			textEscaped(msg)
		}
	}
	d.closeText = "确定"
	d.build()
	val s = d.modal.toString()
	context.writeHtml(s)
}

fun HttpScope.html(block: HtmlDoc.() -> Unit) {
	val h = HtmlDoc(context)
	h.block()
	h.bodyTag?.filterDeep { it.tagName == "a" || it.tagName == "button" }?.forEach {
		val s = when {
			it.href.isNotEmpty() -> it.href
			it.dataUrl.isNotEmpty() -> it.dataUrl
			else -> ""
		}
		if (s.isNotEmpty()) {
			if (!context.allow(s)) {
				it.addClass("d-none")
			}
		}
	}

	context.writeHtml(h.toString())
}

fun HttpScope.formDialog(title: String, block: (Tag) -> Unit) {
	val d = DialogBuild(context)
	d.modal.outputScript = true
	d.title(title)
	d.bodyBlock = {
		block(it)
	}
	d.closeText = "取消"
	d.buttonsBlock = {
		it.button {
			+"提交"
			classList += "m-1"
			btnPrimary()
			onclick = "yet.submitDialogPanel(this);"
		}
	}
	d.build()
	context.writeHtml(d.modal.toString())
}

fun HttpScope.formDialogDisplay(title: String, block: (Tag) -> Unit) {
	val d = DialogBuild(context)
	d.modal.outputScript = true
	d.title(title)
	d.bodyBlock = {
		block(it)
	}
	d.closeText = "确定"
	d.build()
	val s = d.modal.toString()
	context.writeHtml(s)
}