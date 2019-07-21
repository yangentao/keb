@file:Suppress("unused")

package dev.entao.keb.page.ex

import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.R
import dev.entao.keb.page.html.*
import dev.entao.keb.page.widget.DialogBuild
import dev.entao.keb.page.widget.button

fun HttpScope.formErrorDialog(title: String, msg: String) {
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

fun HttpScope.bootPage(block: HtmlDoc.() -> Unit) {
	html {
		head {
			metaCharset("UTF-8")

			meta {
				name = "viewport"
				content = "width=device-width, initial-scale=1, shrink-to-fit=no"
			}
			linkStylesheet(R.CSS.boot)
			linkStylesheet(R.CSS.awesome)
			linkStylesheet(resUri(R.myCSS))
		}
		body {

			scriptLink(resUri(R.jquery))
			scriptLink(R.JS.popper)
			scriptLink(R.JS.boot)
			scriptLink("https://buttons.github.io/buttons.js")
			scriptLink(resUri(R.myJS))


		}
		this.block()
	}
}