package dev.entao.keb.page.widget

import dev.entao.keb.core.HttpContext
import dev.entao.keb.page.B
import dev.entao.keb.page.tag.*

fun installDialogs(tag: Tag) {
	tag.apply {
		div {
			id = "dialogPanel"
		}
		div {
			id = "confirmDlgPanel"
			val b = DialogBuild(httpContext)
			b.title("确认")
			b.bodyBlock = {
				it.p {
					+"确认内容"
				}
			}
			b.closeText = "取消"
			b.buttonsBlock = {
				it.button {
					btnPrimary()
					+"确定"
				}
			}
			b.build()
			tag(b.modal)
		}
		div {
			id = "alertDlgPanel"
			val b = DialogBuild(httpContext)
			b.title("提示")
			b.bodyBlock = {
				it.p {
					+"提示内容"
				}
			}
			b.closeText = "关闭"
			b.build()
			tag(b.modal)
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
			classList += "modal"
			tabindex = "-1"
			role = "dialog"
			div {
				classList += "modal-dialog"
				role = "document"
				div {
					classList += "modal-content"
					div {
						classList += "modal-header"
						h5 {
							classList += "modal-title"
							titleBlock(this)
						}
						button {
							classList += "close"
							dataDismiss = "modal"
							span {
								textUnsafe("&times;")
							}
						}
					}
					div {
						classList += "modal-body"
						//body here
						bodyBlock(this)
					}
					div {
						classList += "modal-footer"
						button {
							classList += B.btnSecondary
							dataDismiss = "modal"
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