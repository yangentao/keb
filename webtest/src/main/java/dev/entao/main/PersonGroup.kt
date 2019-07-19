package dev.entao.main

import dev.entao.kava.base.Label
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.page.LinkItem
import dev.entao.keb.page.ex.Html
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.*
import dev.entao.keb.page.templates.SidebarPage

class PersonGroup(context: HttpContext) : HttpGroup(context) {

	@Label("添加")
	fun addAction() {
		writeHtml(SidebarPage(context)) {
			this.title = "Hello Yang"
			this.buildTopMenu(listOf(::addAction, ::delAction))
			this.navItems = listOf(
					LinkItem("首页", "/a/b"),
					LinkItem("首页2", "/a/b2"),
					LinkItem("首页3", "/a/b3"),
					LinkItem("首页4", "/a/b4"),
					LinkItem("首页5", "/a/b5")
			)
			this.pageBlock = {
				p {
					+"Hello Yang En Tao "
				}
			}
		}
	}

	@Label("删除")
	fun delAction() {
		writeHtml(Html(context)) {
			head {
				title("Hello")
			}
			body {
				p {
					+"I'm Body! "
				}
			}
		}
	}
}