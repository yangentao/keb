@file:Suppress("unused")

package dev.entao.keb.page

import dev.entao.keb.page.widget.configUpload
import dev.entao.kava.base.firstParamName
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.html.*

fun HttpScope.boot(block: HtmlDoc.() -> Unit) {
	html {
		head.apply {
			metaCharset("UTF-8")

			meta {
				name = "viewport"
				content = "width=device-width, initial-scale=1, shrink-to-fit=no"
			}
			linkStylesheet(R.CSS.boot)
			linkStylesheet(httpContext.path.uriRes(R.myCSS))
		}
		body.apply {
			scriptLink(resUri(R.jquery))
			scriptLink(R.JS.popper)
			scriptLink(R.JS.boot)
			scriptLink(resUri(R.myJS))
			if (FilesPage::class in httpContext.filter.routeManager.allGroups) {
				val uploadUri = httpContext.path.action(FilesPage::uploadAction).uri
				val viewUri = httpContext.path.action(FilesPage::imgAction).uri
				val viewParam = FilesPage::imgAction.firstParamName ?: "id"
				val missImg = httpContext.path.uriRes(R.fileImageDefault)
				configUpload(uploadUri, viewUri, viewParam, 30, missImg)
			}
		}
		this.block()
	}
}

fun HttpScope.bootPage(block: Tag.() -> Unit) {
	boot {
		head.apply {
			title(context.filter.webConfig.appName)
		}
		body.apply {
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