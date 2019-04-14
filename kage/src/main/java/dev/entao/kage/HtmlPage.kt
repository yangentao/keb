@file:Suppress("unused")

package dev.entao.kage

import dev.entao.kage.widget.button
import dev.entao.ken.HttpContext
import dev.entao.ken.HttpPage
import dev.entao.sql.SQLQuery

open class HtmlPage(context: HttpContext) : HttpPage(context) {

	fun SQLQuery.limitPage() {
		val n = context.httpParams.int(P.pageN) ?: 0
		this.limit(P.pageSize, n * P.pageSize)
	}

	fun formError(title: String, msg: String) {
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
		htmlSender.text(s)
	}

	fun html(block: HtmlDoc.() -> Unit) {
		val h = HtmlDoc(context)
		h.block()
		h.body.filterDeep { it.tagName == "a" || it.tagName == "button" }.forEach {
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

		htmlSender.text(h.toString())
	}

	fun formDialog(title: String, block: (Tag) -> Unit) {
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
		htmlSender.text(d.modal.toString())
	}

	fun formDialogDisplay(title: String, block: (Tag) -> Unit) {
		val d = DialogBuild(context)
		d.modal.outputScript = true
		d.title(title)
		d.bodyBlock = {
			block(it)
		}
		d.closeText = "确定"
		d.build()
		val s = d.modal.toString()
		htmlSender.text(s)
	}
}