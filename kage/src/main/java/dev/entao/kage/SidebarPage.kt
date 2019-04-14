package dev.entao.kage

import dev.entao.kage.TConst.name
import dev.entao.kage.widget.a
import dev.entao.kage.widget.button
import dev.entao.kage.widget.configUpload
import dev.entao.kbase.hasAnnotation
import dev.entao.kbase.userLabel
import dev.entao.ken.anno.IndexAction
import dev.entao.kbase.firstParamName
import dev.entao.kbase.removeAllIf
import dev.entao.ken.*
import dev.entao.ken.ex.pages.FilesPage
import kotlin.reflect.KClass

fun HtmlPage.sidebarPage(block: Tag.() -> Unit) {
	val config = context.filter.webConfig
	html {
		head.apply {
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
		body.apply {
			nav {
				clazz = "navbar navbar-expand-md navbar-dark fixed-left"
				a {
					clazz = "navbar-brand"
					href = context.path.uriRoot
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
				checkAlertMessage(this)
				this.block()
			}
			installDialogs(this)

			scriptLink(resUri(R.jquery))
			scriptLink(R.JS.popper)
			scriptLink(R.JS.boot)
			scriptLink("https://buttons.github.io/buttons.js")
			scriptLink(resUri(R.myJS))
			if (FilesPage::class in httpContext.filter.allPages) {
				val uploadUri = httpContext.path.action(FilesPage::uploadAction).uri
				val viewUri = httpContext.path.action(FilesPage::imgAction).uri
				val viewParam = FilesPage::imgAction.firstParamName ?: "id"
				val missImg = httpContext.path.uriRes(R.fileImageDefault)
				configUpload(uploadUri, viewUri, viewParam, 30, missImg)
			}
		}

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

fun HtmlPage.checkAlertMessage(tag: Tag) {
	val er = httpParams.str(ParamConst.ERROR) ?: ""
	if (er.isNotEmpty()) {
		tag.alertError { +er }
	}
	val m = httpParams.str(ParamConst.SUCCESS) ?: ""
	if (m.isNotEmpty()) {
		tag.alertSuccess { +m }
	}
}

private fun HtmlPage.buildUserInfoFlex(parentTag: Tag) {
	val user = context.account
	val cfg = context.filter.webConfig
	parentTag.flex {
		classList += B.Flex.justifyContentBetween
		classList += "py-4"
		classList += "text-white"
		classList += "w-100"
		span {
			classList += "mr-auto"
			if (user != null) {
				+user.name
			} else {
				+"未登录"
			}
		}
		if (user == null) {
			a("登录", cfg.loginUri)
		} else {
			a("登出", cfg.logoutUri)
		}
	}
}

private fun HttpPage.buildTopActionMenu(parentTag: Tag) {
	val ls = actionItems
	if (ls.isEmpty()) {
		return
	}
	parentTag.navPills {
		actionItems.forEach {
			navLink(it, null)
		}
	}
}

private fun HttpPage.navLinks(): ArrayList<LinkItem> {
	val currUri = context.request.requestURI
	val navConList = ArrayList<Pair<String, KClass<*>>>(filter.navControlerList)

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

private fun HttpPage.makeLinkItem(c: Pair<String, KClass<*>>, currUri: String): LinkItem {
	val s = WebPath(filter).append(c.second.pageName).uri
	val indexAction = c.second.actionList.firstOrNull { it.hasAnnotation<IndexAction>() }
	val ss = if (indexAction != null) {
		path.action(indexAction).uri
	} else {
		s
	}
	return LinkItem(c.second.userLabel, ss, isSubpath(currUri, s))
}

class LinkItem(val label: String, val url: String, var active: Boolean = false) {
	val children: ArrayList<LinkItem> = ArrayList()

}
