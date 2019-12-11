@file:Suppress("unused")

package dev.entao.keb.page

import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.bootstrap.buttonPrimary
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.DialogBuild


fun HttpScope.formErrorDialog(title: String, msg: String) {
	val d = DialogBuild(context)
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

fun HttpScope.formDialog(title: String, block: (Tag) -> Unit) {
	val d = DialogBuild(context)
	d.title(title)
	d.bodyBlock = {
		block(it)
	}
	d.closeText = "取消"
	d.buttonsBlock = {
		it.buttonPrimary(class_ to "m-1") {
			+"提交"
			this[onclick_] = "yet.submitDialogPanel(this);"
		}
	}
	d.build()
	context.sendHtmlTag(d.modal)
}

fun HttpScope.formDialogDisplay(title: String, block: (Tag) -> Unit) {
	val d = DialogBuild(context)
	d.title(title)
	d.bodyBlock = {
		block(it)
	}
	d.closeText = "确定"
	d.build()
	context.sendHtmlTag(d.modal)
}
