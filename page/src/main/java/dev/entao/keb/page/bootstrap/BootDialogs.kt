package dev.entao.keb.page.bootstrap

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.tag.*

fun installDialogs(tag: Tag) {
	tag.apply {
		div(id_ to "dialogPanel") {

		}
		div(id_ to "confirmDlgPanel") {
			val b = ModalDialog(httpContext)
			b.modalTitleText("确认")
			b.modalBody {
				p {
					+"确认内容"
				}
			}
			b.modalFooter {
				buttonPrimary {
					+"确定"
				}
			}
			b.closeText("取消")
			add(b)
		}
		div(id_ to "alertDlgPanel") {
			val b = ModalDialog(httpContext)
			b.modalTitleText("提示")
			b.modalBody {
				p {
					+"提示内容"
				}
			}
			b.closeText("关闭")
			add(b)
		}
	}
}


class ModalDialog(context: HttpContext) : Tag(context, "div") {

	lateinit var modalHeader: Tag
	lateinit var modalBody: Tag
	lateinit var modalFooter: Tag
	lateinit var modalHeaderTitle: Tag
	lateinit var closeButton: Tag

	init {
		this += "modal"
		this[tabindex_] = "-1"
		this[role_] = "dialog"
		div(class_ to _modal_dialog.._modal_dialog_centered, role_ to V.document) {
			div(class_ to _modal_content) {
				modalHeader = div(class_ to _modal_header) {
					modalHeaderTitle = h5(class_ to _modal_title) {
					}
					buttonB(class_ to _close, data_dismiss_ to "modal") {
						span {
							textUnsafe("&times;")
						}
					}
				}
				modalBody = div(class_ to "modal-body") {
				}
				modalFooter = div(class_ to "modal-footer") {
					closeButton = buttonB(class_ to _btn_secondary, data_dismiss_ to "modal") {
						+"关闭"
					}
				}
			}
		}

	}

	fun closeText(text: String) {
		val a = closeButton.children.first() as TextEscaped
		a.text = text
	}

	fun modalTitleText(titleText: String) {
		modalHeaderTitle.textEscaped(titleText)
	}

	fun modalBody(block: TagCallback) {
		modalBody.block()
	}

	fun modalFooter(block: TagCallback) {
		modalFooter.block()
	}
}

fun Tag.modalDialog(block: ModalDialog.() -> Unit) {
	val d = ModalDialog(this.httpContext)
	add(d)
	d.block()
}

fun HttpScope.dialogAlert(title: String, msg: String) {
	val d = ModalDialog(context)
	d.modalTitleText(title)
	d.modalBody {
		p {
			textEscaped(msg)
		}
	}
	d.closeText("确定")
	context.sendHtmlTag(d)
}

fun HttpScope.dialogForm(title: String, bodyBlock: Tag.() -> Unit) {
	val d = ModalDialog(context)
	d.modalTitleText(title)
	d.modalBody(bodyBlock)
	d.closeText("取消")
	d.modalFooter {
		buttonPrimary(class_ to "m-1") {
			+"提交"
			this[onclick_] = "dialogSubmitReload(this);"
		}
	}
	context.sendHtmlTag(d)
}

