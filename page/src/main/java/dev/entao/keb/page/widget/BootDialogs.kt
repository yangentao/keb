package dev.entao.keb.page.widget

import dev.entao.keb.core.HttpContext
import dev.entao.keb.page.bootstrap.buttonB
import dev.entao.keb.page.bootstrap.buttonPrimary
import dev.entao.keb.page.tag.*

fun installDialogs(tag: Tag) {
	tag.apply {
		div(id_ to "dialogPanel") {

		}
		div(id_ to "confirmDlgPanel") {
			val b = DialogBuild(httpContext)
			b.title("确认")
			b.bodyBlock = {
				it.p {
					+"确认内容"
				}
			}
			b.closeText = "取消"
			b.buttonsBlock = {
				it.buttonPrimary {
					+"确定"
				}
			}
			b.build()
			add(b.modal)
		}
		div(id_ to "alertDlgPanel") {
			val b = DialogBuild(httpContext)
			b.title("提示")
			b.bodyBlock = {
				it.p {
					+"提示内容"
				}
			}
			b.closeText = "关闭"
			b.build()
			add(b.modal)
		}
	}
}

class DialogBuild(context: HttpContext) {
	val modal = Tag(context, "div")

	var closeText: String = "关闭"

	var titleBlock: (Tag) -> Unit = { it.textEscaped("Title") }
	var bodyBlock: (Tag) -> Unit = { it.p { +"Body" } }

	var buttonsBlock: (Tag) -> Unit = {}

	fun title(titleText: String) {
		titleBlock = { it.textEscaped(titleText) }
	}

	fun build(): Tag {
		modal.apply {
			this += "modal"
			this[tabindex_] = "-1"
			this[role_] = "dialog"
			div(class_ to _modal_dialog, role_ to V.document) {
				div(class_ to _modal_content) {
					div(class_ to _modal_header) {
						h5(class_ to _modal_title) {
							titleBlock(this)
						}
						buttonB(class_ to _close, data_dismiss_ to "modal") {
							span {
								textUnsafe("&times;")
							}
						}
					}
					div(class_ to "modal-body") {
						bodyBlock(this)
					}
					div(class_ to "modal-footer") {
						buttonB(class_ to _btn_secondary, data_dismiss_ to "modal") {
							+closeText
						}
						buttonsBlock(this)
					}
				}
			}
		}
		return modal
	}
}