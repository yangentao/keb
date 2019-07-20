package dev.entao.keb.am

import dev.entao.kava.base.Label
import dev.entao.kava.base.userLabel
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.page.*
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.*
import dev.entao.keb.page.templates.SidebarPage

@Label("员工")
class IndexGroup(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		addAction()
	}

	private fun buildNavBar(ptag: Tag) {
		val acList = listOf(::listAction, ::addAction)
		ptag.navbarDark("APP管理") {
			navbarLeft {
				acList.forEach {
					val u = context.actionUri(it)
					navbarItemLink(it.userLabel, u, context.currentUri == u)
				}
			}
			navbarRight {
				navbarItemLink("登录", "#")
			}
		}
	}

	private fun writePage(block: TagCallback) {
		writeHtml(AppPage(context)) {
			html.head {
				title("APP管理")
			}
			html.body {
				divContainer {
					buildNavBar(this)
					this.block()
				}
			}
		}
	}

	@Label("列表")
	fun listAction() {
		writePage {
			p {
				+" List  "
			}
		}
	}

	@Label("添加")
	fun addAction() {
		writePage {
			p {
				+" ADD "
			}
		}

	}

}