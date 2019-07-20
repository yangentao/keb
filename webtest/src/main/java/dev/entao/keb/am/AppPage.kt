package dev.entao.keb.am

import dev.entao.kava.base.firstParamName
import dev.entao.keb.core.HttpContext
import dev.entao.keb.page.FilesPage
import dev.entao.keb.page.R
import dev.entao.keb.page.html.body
import dev.entao.keb.page.widget.configUpload

class AppPage(context: HttpContext) : BootTemplate(context){


	init {

		html.body {
			if (FilesPage::class in httpContext.filter.routeManager.allGroups) {
				val uploadUri = httpContext.actionUri(FilesPage::uploadAction)
				val viewUri = httpContext.actionUri(FilesPage::imgAction)
				val viewParam = FilesPage::imgAction.firstParamName ?: "id"
				val missImg = httpContext.resUri(R.fileImageDefault)
				configUpload(uploadUri, viewUri, viewParam, 30, missImg)
			}
		}
	}

}
