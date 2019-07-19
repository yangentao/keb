package dev.entao.main

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpFilter
import dev.entao.keb.page.html.*
import javax.servlet.annotation.WebFilter

@WebFilter(urlPatterns = ["/y/*"])
class MainFilter : HttpFilter() {

	override fun onInit() {

	}

	fun helloAction(context: HttpContext) {
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