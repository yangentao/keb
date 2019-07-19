package dev.entao.main

import dev.entao.kava.base.Label
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.IndexAction
import dev.entao.keb.page.LinkItem
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.p
import dev.entao.keb.page.templates.SidebarPage

@Label("员工")
class PersonGroup(context: HttpContext) : HttpGroup(context) {

	@Label("添加")
	fun addAction() {
		writeHtml(SidebarPage(context)) {
			this.title = "Hello Yang"
			this.buildTopMenu(listOf(::addAction, ::delAction))
			this.buildLeftMenu(listOf(PersonGroup::class, SaleGroup::class))

			this.pageBlock = {
				p {
					+"Hello Yang En Tao "
				}
			}
		}
	}

	@Label("删除")
	fun delAction() {
		writeHtml(SidebarPage(context)) {
			this.title = "Hello Yang"
			this.buildTopMenu(listOf(::addAction, ::delAction))
			this.buildLeftMenu(listOf(PersonGroup::class, SaleGroup::class))

			this.pageBlock = {
				p {
					+" delete me  "
				}
			}
		}
	}
}