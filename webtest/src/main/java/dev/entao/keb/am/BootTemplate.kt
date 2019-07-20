package dev.entao.keb.am

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.R
import dev.entao.keb.page.ex.HtmlTemplate
import dev.entao.keb.page.html.*

open class BootTemplate(context: HttpContext) : HttpScope(context), HtmlTemplate {
	val html = HtmlDoc(context)

	init {
		html.head {
			metaCharset("UTF-8")
			meta {
				httpEquiv = "X-UA-Compatible"
				content = "IE=edge"
			}
			meta {
				name = "viewport"
				content = "width=device-width, initial-scale=1, shrink-to-fit=no"
			}
			linkStylesheet(R.CSS.boot)
			linkStylesheet(R.CSS.awesome)
			linkStylesheet(resUri(R.myCSS))
		}
		html.body {
			scriptLink(resUri(R.jquery))
			scriptLink(R.JS.popper)
			scriptLink(R.JS.boot)
			scriptLink("https://buttons.github.io/buttons.js")
			scriptLink(resUri(R.myJS))
		}
	}



	override fun toHtml(): String {
		return html.toHtml()
	}

}