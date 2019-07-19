package dev.entao.main

import dev.entao.kava.base.Label
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.page.LinkItem
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.p
import dev.entao.keb.page.templates.SidebarPage

@Label("销售")
class SaleGroup(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		addAction()
	}

	@Label("添加")
	fun addAction() {
		writeHtml(SidebarPage(context)) {
			this.title = "销售"
			this.buildTopMenu(listOf(::addAction, ::delAction))
			this.buildLeftMenu(listOf(PersonGroup::class, SaleGroup::class))

			this.pageBlock = {
				p {
					+"销售 添加"
				}
			}
		}
	}

	@Label("删除")
	fun delAction() {
		writeHtml(SidebarPage(context)) {
			this.title = "销售"
			this.buildTopMenu(listOf(::addAction, ::delAction))
			this.buildLeftMenu(listOf(PersonGroup::class, SaleGroup::class))

			this.pageBlock = {
				p {
					+" 销售删除 "
				}
			}
		}
	}
}