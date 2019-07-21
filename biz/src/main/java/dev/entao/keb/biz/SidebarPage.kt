package dev.entao.keb.biz

import dev.entao.kava.base.removeAllIf
import dev.entao.kava.base.userLabel
import dev.entao.keb.core.HttpScope
import dev.entao.keb.core.isSubpath
import dev.entao.keb.page.*
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.a
import dev.entao.keb.page.widget.button
import dev.entao.keb.page.widget.flex
import dev.entao.keb.page.widget.installDialogs
import kotlin.reflect.KClass

fun HttpScope.sidebarPage(block: Tag.() -> Unit) {
	val config = context.filter.webConfig

	html {
		head {
			metaCharset("UTF-8")
			meta {
				httpEquiv = "X-UA-Compatible"
				content = "IE=edge"
			}
			meta {
				name = "viewport"
				content = "width=device-width, initial-scale=1, shrink-to-fit=no"
			}
			title(config.appName)
			linkStylesheet(R.CSS.boot)
			linkStylesheet(R.CSS.awesome)
			linkStylesheet(resUri(R.navbarLeft))
//			linkStylesheet(resUri(R.navbarRight))
			linkStylesheet(resUri(R.myCSS))
			val icon = config.favicon
			if (icon.isNotEmpty()) {
				link {
					rel = "shortcut icon"
					if (icon.toLowerCase().endsWith(".png")) {
						type = "image/png"
					} else {
						type = "image/jpeg"
					}
					href = icon
				}
			}
		}
		body {
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
						for (item in this@sidebarPage.navLinks()) {
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
				buildTopActionMenu(this)
				showMessagesIfPresent()
				this.block()
			}
			installDialogs(this)

			scriptLink(resUri(R.jquery))
			scriptLink(R.JS.popper)
			scriptLink(R.JS.boot)
			scriptLink("https://buttons.github.io/buttons.js")
			scriptLink(resUri(R.myJS))

		}

	}
}

private fun HttpScope.buildUserInfoFlex(parentTag: Tag) {
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

@Suppress("UNUSED_PARAMETER")
private fun HttpScope.buildTopActionMenu(parentTag: Tag) {
//	val ls = actionItems
//	if (ls.isEmpty()) {
//		return
//	}
//	parentTag.navPills {
//		actionItems.forEach {
//			navLink(it, null)
//		}
//	}
}

private fun HttpScope.navLinks(): ArrayList<LinkItem> {
	val currUri = context.currentUri
	val navConList = ArrayList<Pair<String, KClass<*>>>(context.filter.navControlerList)

	val linkList = ArrayList<LinkItem>()

	while (navConList.isNotEmpty()) {
		val first = navConList.removeAt(0)
		val ls2 = navConList.removeAllIf {
			it.first == first.first
		}
		if (ls2.isEmpty()) {
			val a = makeLinkItem(first, currUri)
			if (context.allow(a.url)) {
				linkList += a
			}
			continue
		}
		val item = LinkItem(first.first, "#", false)
		val b = makeLinkItem(first, currUri)
		if (context.allow(b.url)) {
			item.children += b
		}
		for (c in ls2) {
			val dd = makeLinkItem(c, currUri)
			if (context.allow(dd.url)) {
				item.children += dd
			}
		}
		item.active = item.children.any { it.active }
		if (item.children.isNotEmpty()) {
			linkList += item
		}
	}
	return linkList
}

private fun HttpScope.makeLinkItem(c: Pair<String, KClass<*>>, currUri: String): LinkItem {
	val s = context.groupUri(c.second)
	return LinkItem(c.second.userLabel, s, isSubpath(currUri, s))
}

