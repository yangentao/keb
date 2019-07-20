package dev.entao.keb.am

import dev.entao.kava.base.userLabel
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.LinkItem
import dev.entao.keb.page.R
import dev.entao.keb.page.ex.HtmlTemplate
import dev.entao.keb.page.html.*
import dev.entao.keb.page.navPills
import dev.entao.keb.page.widget.a

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

	fun buildNavPills(ptag: Tag, actionList: List<HttpAction>) {
		val topItems = buildTopMenu(actionList)
		ptag.apply {
			navPills {
				for (ti in topItems) {
					a {
						addClass("nav-link")
						if (ti.active) {
							addClass("active")
						}
						href = ti.url
						+ti.label
					}
				}
			}
		}
	}

	fun buildTopMenu(actionList: List<HttpAction>): List<LinkItem> {
		val c = context.currentUri
		return actionList.map {
			val a = context.actionUri(it)
			LinkItem(it.userLabel, a, c == a)
		}
	}

	override fun toHtml(): String {
		return html.toHtml()
	}

}