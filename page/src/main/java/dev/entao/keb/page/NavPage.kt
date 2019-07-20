@file:Suppress("unused")

package dev.entao.keb.page

import dev.entao.keb.page.widget.configUpload
import dev.entao.kava.base.firstParamName
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.html.*

fun HttpScope.boot(block: HtmlDoc.() -> Unit) {
	html {
		head {
			metaCharset("UTF-8")

			meta {
				name = "viewport"
				content = "width=device-width, initial-scale=1, shrink-to-fit=no"
			}
			linkStylesheet(R.CSS.boot)
			linkStylesheet(httpContext.resUri(R.myCSS))
		}
		body {
			scriptLink(resUri(R.jquery))
			scriptLink(R.JS.popper)
			scriptLink(R.JS.boot)
			scriptLink(resUri(R.myJS))
			if (FilesPage::class in httpContext.filter.routeManager.allGroups) {
				val uploadUri = httpContext.actionUri(FilesPage::uploadAction)
				val viewUri = httpContext.actionUri(FilesPage::imgAction)
				val viewParam = FilesPage::imgAction.firstParamName ?: "id"
				val missImg = httpContext.resUri(R.fileImageDefault)
				configUpload(uploadUri, viewUri, viewParam, 30, missImg)
			}
		}
		this.block()
	}
}

fun HttpScope.bootPage(block: Tag.() -> Unit) {
	boot {
		head {
			title(context.filter.webConfig.appName)
		}
		body {
			divContainerFluid {
				this.block()
			}
		}
	}
}

fun HttpScope.cardPage(block: Tag.() -> Unit) {
	sidebarPage {
		card {
			this.block()
		}
	}
}

fun HttpScope.cardBodyPage(title: String, block: Tag.() -> Unit) {
	sidebarPage {
		card {
			cardHeader(title)
			cardBody {
				this.block()
			}
		}
	}
}
