package dev.entao.keb.page.groups

import dev.entao.kava.base.userLabel
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.*
import dev.entao.keb.page.HtmlTemplate
import dev.entao.keb.page.showMessagesIfPresent
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.*
import kotlin.reflect.KClass

class SidebarTemplate(override val context: HttpContext) : HttpScope, HtmlTemplate {
	val html = HtmlDoc(context)
	var title: String = context.filter.webConfig.appName

	var navItems: List<LinkItem> = emptyList()
	var topItems: List<LinkItem> = emptyList()

	var pageBlock: Tag.() -> Unit = {}

	private fun build() {
		val config = context.filter.webConfig
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
			title(title)
			linkStylesheet(R.CSS.boot)
			linkStylesheet(R.CSS.awesome)
			linkStylesheet(resUri(R.navbarLeft))
			linkStylesheet(resUri(R.myCSS))
			val icon = config.favicon
			if (icon.isNotEmpty()) {
				link {
					rel = "shortcut icon"
					type = if (icon.toLowerCase().endsWith(".png")) {
						"image/png"
					} else {
						"image/jpeg"
					}
					href = icon
				}
			}
		}
		html.body {
			nav {
				clazz = "navbar navbar-expand-md navbar-dark fixed-left"
				a {
					clazz = "navbar-brand"
					href = context.rootUri
					+config.appName
				}
				button {
					clazz = "navbar-toggler"
					type = "button"
					dataToggle = "collapse"
					dataTarget = "#navbarsExampleDefault"
					ariaControls = "navbarsExampleDefault"
					ariaExpanded = "false"
					ariaLabel = "展开"
					span {
						clazz = "navbar-toggler-icon"
					}
				}
				div {
					id = "navbarsExampleDefault"
					clazz = "collapse navbar-collapse"
					buildUserInfoFlex(this)
					ul {
						clazz = "navbar-nav"
						for (item in navItems) {
							li {
								clazz = "nav-item"
								if (item.active) {
									classList += "active"
								}
								val itemA = a {
									clazz = "nav-link"
									href = item.url
									+item.label
								}
								if (item.children.isNotEmpty()) {
									itemA.dataToggle = "collapse"
									ul {
										clazz = "collapse"
										if (item.active) {
											classList += "show"
										}
										this.needId()
										itemA.href = "#${this.id}"

										for (X in item.children) {
											li {
												clazz = "nav-item"
												if (X.active) {
													classList += "active"
												}
												a {
													clazz = "nav-link"
													href = X.url
													+X.label
												}
											}
										}

									}
								}
							}
						}
					}
				}
			}
			div {
				clazz = "container-fluid"
				if (topItems.isNotEmpty()) {
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
				showMessagesIfPresent()
				this.pageBlock()
			}
			installDialogs(this)

			scriptLink(resUri(R.jquery))
			scriptLink(R.JS.popper)
			scriptLink(R.JS.boot)
			scriptLink("https://buttons.github.io/buttons.js")
			scriptLink(resUri(R.myJS))

		}
	}

	private fun buildUserInfoFlex(parentTag: Tag) {
		parentTag.flex {
			classList += B.Flex.justifyContentBetween
			classList += "py-4"
			classList += "text-white"
			classList += "w-100"
			span {
				classList += "mr-auto"
				if (context.isLogined) {
					+context.accountName
				} else {
					+"未登录"
				}
			}
			if (context.isLogined) {
				val u = httpContext.filter.loginUri
				if (u.isNotEmpty()) {
					a("登录", u)
				}
			} else {
				val u = httpContext.filter.logoutUri
				if (u.isNotEmpty()) {
					a("登出", u)
				}
			}
		}
	}

	fun buildTopMenu(actionList: List<HttpAction>) {
		val c = context.currentUri
		this.topItems = actionList.map {
			val a = context.actionUri(it)
			LinkItem(it.userLabel, a, c == a)
		}
	}

	fun buildLeftMenu(ls: List<KClass<out HttpGroup>>) {
		val c = context.currentUri
		this.navItems = ls.map {
			val a = context.groupUri(it)
			LinkItem(it.userLabel, a, c.startsWith(a))
		}
	}

	override fun toHtml(): String {
		build()
		return html.toHtml()
	}

}