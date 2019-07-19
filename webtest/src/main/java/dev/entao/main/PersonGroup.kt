package dev.entao.main

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.page.ex.Html
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.*
import dev.entao.keb.page.templates.SidebarPage

class PersonGroup(context: HttpContext) : HttpGroup(context) {

	fun addAction() {
		writeHtml(SidebarPage(context)) {
			this.title = "Hello Yang"
			this.pageBlock = {
				p {
					+"Hello Yang En Tao "
				}
			}
		}
	}

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