package dev.entao.keb.page.groups

import dev.entao.kava.base.userLabel
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.account.accountName
import dev.entao.keb.core.account.isAccountLogined
import dev.entao.keb.page.LinkItem
import dev.entao.keb.page.R
import dev.entao.keb.page.bootstrap.buttonB
import dev.entao.keb.page.showMessagesIfPresent
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.*
import kotlin.reflect.KClass

class SidebarTemplate(context: HttpContext) : HtmlPage(context) {
	var title: String = context.filter.appName

	var navItems: List<LinkItem> = emptyList()
	var topItems: List<LinkItem> = emptyList()

	var pageBlock: Tag.() -> Unit = {}

	private fun build() {
		setupBootstrap()
		this.head.link(rel_ to "stylesheet", href_ to context.resUri(R.navbarLeft))
		setupMyCssJs()
		head {
			title(title)
		}
		body {
			nav(class_ to _navbar.._navbar_expand_md.._navbar_dark.."fixed-left") {
				a(id_ to _navbar_brand, href_ to context.rootUri) {
					+context.filter.appName
				}
				buttonB(class_ to "navbar-toggler", aria_expanded_ to "false", aria_label_ to "展开", aria_controls_ to "navbarsExampleDefault",
						data_toggle_ to "collapse", data_target_ to "#navbarsExampleDefault") {
					span(class_ to _navbar_toggler_icon) {

					}
				}
				div(id_ to "navbarsExampleDefault", class_ to _collapse.._navbar_collapse) {
					buildUserInfoFlex(this)
					ul(class_ to _navbar_nav) {
						for (item in navItems) {
							li(class_ to _nav_item) {
								if (item.active) {
									this += _active
								}
								val itemA = a(class_ to _nav_link, href_ to item.url) {
									+item.label
								}
								if (item.children.isNotEmpty()) {
									itemA[data_toggle_] = "collapse"
									ul(class_ to _collapse) {
										if (item.active) {
											this += _show
										}
										this.needId()
										itemA[href_] = "#${this[id_]}"

										for (X in item.children) {
											li(class_ to _nav_item) {
												if (X.active) {
													this += _active
												}
												a(class_ to _nav_link, href_ to X.url) {
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
			div(class_ to _container_fluid) {
				if (topItems.isNotEmpty()) {
					navPills {
						for (ti in topItems) {
							a(class_ to _nav_link, href_ to ti.url) {
								if (ti.active) {
									this += _active
								}
								+ti.label
							}
						}
					}
				}
				showMessagesIfPresent()
				this.pageBlock()
			}
			installDialogs(this)
		}
	}

	private fun buildUserInfoFlex(parentTag: Tag) {
		parentTag.flex(class_ to _justify_content_between.._py_4.._text_white.._w_100) {
			span(class_ to _mr_auto) {
				if (context.isAccountLogined) {
					+context.accountName
				} else {
					+"未登录"
				}
			}
			if (context.isAccountLogined) {
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
			val a = context.filter.actionUri(it)
			LinkItem(it.userLabel, a, c == a)
		}
	}

	fun buildLeftMenu(ls: List<KClass<out HttpGroup>>) {
		val c = context.currentUri
		this.navItems = ls.map {
			val a = context.filter.groupUri(it)
			LinkItem(it.userLabel, a, c.startsWith(a))
		}
	}

}