package dev.entao.main

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpFilter
import dev.entao.keb.page.html.*
import javax.servlet.annotation.WebFilter

@WebFilter(urlPatterns = ["/*"])
class MainFilter : HttpFilter() {

	override fun onInit() {

		addGroup(PersonGroup::class, SaleGroup::class)
	}

	fun indexAction(context: HttpContext) {
		val h = HtmlDoc(context)
		h.head {
			title("Hello Title")
		}
		h.body {
			p {
				+"Yang En Tao "
			}
		}
		context.writeHtml(h.toString())
	}
}