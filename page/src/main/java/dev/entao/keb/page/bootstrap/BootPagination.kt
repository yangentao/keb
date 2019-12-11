package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.P
import dev.entao.keb.page.S
import dev.entao.keb.page.tag.*
import kotlin.math.min


//currentPage从0开始
fun Tag.paginationBuild(pageCount: Int, currentPage: Int) {
	if (pageCount <= 0) {
		return
	}
	val HP: Int = 6

	nav {
		ul(class_ to _pagination.._pagination_sm) {

			if (pageCount < 10) {
				for (i in 0 until pageCount) {
					li(class_ to _page_item) {
						if (i == currentPage) {
							this += _active
						}
						a(class_ to _page_link, href_ to "#", P.dataPage to "$i") {
							+"${i + 1}"
						}
					}
				}
			} else {
				li(class_ to _page_item) {
					a(class_ to _page_link, href_ to "#", P.dataPage to "0") {
						+S.firstPage
					}
				}
				val maxP = pageCount - 1
				var fromP = currentPage - HP
				var toP = currentPage + HP
				if (fromP < 0) {
					fromP = 0
				}
				if (toP > maxP) {
					toP = maxP
				}
				if (toP < maxP) {
					val n = min(maxP - toP, HP * 2 - (toP - fromP))
					if (n > 0) {
						toP += n
					}
				}
				if (fromP > 0) {
					val n = min(fromP, HP * 2 - (toP - fromP))
					if (n > 0) {
						fromP -= n
					}
				}
				if (fromP != 0) {
					li(class_ to _page_item) {
						a(class_ to _page_link, href_ to "#", P.dataPage to "${fromP - 1}") {
							+"..."
						}
					}
				}

				for (i in fromP..toP) {
					li(class_ to _page_item) {
						if (i == currentPage) {
							this += _active
						}
						a(class_ to _page_link, href_ to "#", P.dataPage to "$i") {
							+"${i + 1}"
						}
					}
				}
				if (toP != maxP) {
					li(class_ to _page_item) {
						a(class_ to _page_link, href_ to "#", P.dataPage to "${toP + 1}") {
							+"..."
						}
					}
				}

				li(class_ to _page_item) {
					a(class_ to _page_link, href_ to "#", P.dataPage to "$maxP") {
						+S.lastPage
					}
				}
			}


			val ulId = this.needId()
			script {
				"""
					$(function(){
						$('#$ulId').find(".page-link").click(function(e){
							e.preventDefault();
							let dataPage = $(this).attr('${P.dataPage}');
							searchBy('${P.pageArg}', dataPage);
							return false;
						});
					});
				"""
			}
		}
	}

}

fun Tag.paginationByRowCount(rowCount: Int) {
	val pc = (rowCount + P.pageSize - 1) / P.pageSize
	val n = this.httpContext.httpParams.int(P.pageArg) ?: 0
	this.paginationBuild(pc, n)
	this.span {
		+"共 $rowCount 条记录"
	}
}


