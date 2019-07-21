package dev.entao.keb.page.groups

import dev.entao.kava.base.userLabel
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.*
import dev.entao.keb.page.ex.HtmlTemplate
import dev.entao.keb.page.html.*
import dev.entao.keb.page.widget.a
import dev.entao.keb.page.widget.button
import kotlin.reflect.KClass

class SidebarPage(override val context: HttpContext) : HttpScope, HtmlTemplate {

	var title: String = context.filter.webConfig.appName

	var navItems: List<LinkItem> = emptyList()
	var topItems: List<LinkItem> = emptyList()

	var pageBlock: Tag.() -> Unit = {}

	private fun build(html: HtmlDoc) {
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
				checkAlertMessage(this)
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
		val cfg = context.filter.webConfig
		parentTag.flex {
			classList += B.Flex.justifyContentBetween
			classList += "py-4"
			classList += "text-white"
			classList += "w-100"
			span {
				classList += "mr-auto"
				if (context.loginedWeb) {
					+context.accountName
				} else {
					+"未登录"
				}
			}
			if (context.loginedWeb) {
				a("登录", cfg.loginUri)
			} else {
				a("登出", cfg.logoutUri)
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

	private fun installDialogs(tag: Tag) {
		tag.apply {
			div {
				id = "dialogPanel"
			}
			div {
				id = "confirmDlgPanel"
				val b = DialogBuild(httpContext)
				b.title("确认")
				b.bodyBlock = {
					it.p {
						+"确认内容"
					}
				}
				b.closeText = "取消"
				b.buttonsBlock = {
					it.button {
						btnPrimary()
						+"确定"
					}
				}
				b.build()
				addTag(b.modal)
			}
			div {
				id = "alertDlgPanel"
				val b = DialogBuild(httpContext)
				b.title("提示")
				b.bodyBlock = {
					it.p {
						+"提示内容"
					}
				}
				b.closeText = "关闭"
				b.build()
				addTag(b.modal)
			}
		}
	}

	override fun toHtml(): String {
		val html = HtmlDoc(context)
		build(html)
		return html.toHtml()
	}

}