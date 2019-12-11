package dev.entao.keb.page.widget

import dev.entao.keb.page.P
import dev.entao.keb.page.S
import dev.entao.keb.page.tag.*
import kotlin.math.max

fun Tag.flex(vararg vs: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _d_flex) {
		for (p in vs) {
			if (p.first == class_.value) {
				this[class_] += p.second
			} else {
				this[p.first] = p.second
			}
		}
		this.block()
	}
}

fun Tag.flexRow(vararg vs: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _d_flex.._flex_row, *vs, block = block)
}

fun Tag.progress(vararg kv: HKeyValue, block: Tag.() -> Unit): Tag {
	return this.div(class_ to _progress, *kv) {
		this.block()
	}
}

fun Tag.progressBar(block: Tag.() -> Unit): Tag {
	return this.div(class_ to _progress_bar, role_ to V.progressbar) {
		this[aria_valuemin_] = "0"
		this[aria_valuemax_] = "100"
		this.block()
	}
}


fun Tag.navPills(block: TagCallback) {
	nav(class_ to _nav.._nav_pills) {
		this.block()
	}
}




fun Tag.pagination(block: TagCallback) {
	nav {
		ul(id_ to "pagination", class_ to _pagination) {
			this.block()
		}
	}
}

fun Tag.pageItem(block: TagCallback) {
	li(class_ to _page_item) {
		this.block()
	}
}

fun Tag.pageLink(vararg vs: HKeyValue, block: TagCallback) {
	a(class_ to _page_link, href_ to "#", *vs) {
		this.block()
	}
}

fun Tag.pageItemLink(dataPageN: Int, block: TagCallback) {
	pageItem {
		pageLink(P.dataPage to "$dataPageN") {
			this.block()
		}
	}
}

//Yet.makePageCond
//currentPage从0开始
fun Tag.paginationBuild(pageCount: Int, currentPage: Int) {
	if (pageCount <= 0) {
		return
	}
	pagination {
		needId()
		pageItemLink(0) {
			if (currentPage == 0) {
				parent!! += _disabled
			}
			textUnsafe(S.firstPage)
		}
		pageItemLink(max(0, currentPage - 1)) {
			if (currentPage == 0) {
				parent!! += _disabled
			}
			textUnsafe(S.prePage)
		}

		val M = 14
		val M2 = M / 2
		if (pageCount <= M) {
			for (i in 0..(pageCount - 1)) {
				pageItemLink(i) {
					if (i == currentPage) {
						parent!! += _active
					}
					+"${i + 1}"
				}
			}
		} else {
			val from: Int
			val end: Int
			if (currentPage < M2) {
				from = 0
				end = from + M
			} else if (currentPage + M2 >= pageCount - 1) {
				end = pageCount - 1
				from = end - M
			} else {
				from = currentPage - M2
				end = currentPage + M2
			}

			if (from > 0) {
				pageItemLink(from - 1) {
					textUnsafe(S.morePage)
				}
			}
			for (i in from..end) {
				pageItemLink(i) {
					if (i == currentPage) {
						parent!! += _active
					}
					+"${i + 1}"
				}
			}
			if (end < pageCount - 1) {
				pageItemLink(end + 1) {
					textUnsafe(S.morePage)
				}
			}
		}
		pageItemLink(currentPage + 1) {
			if (currentPage >= pageCount - 1) {
				parent!! += _disabled
			}
			textUnsafe(S.nextPage)
		}
		pageItemLink(pageCount - 1) {
			if (currentPage >= pageCount - 1) {
				parent!! += _disabled
			}
			textUnsafe(S.lastPage)
		}

		val ulId = id
		script {
			"""
			Yet.pageN = $currentPage;
			$('#$ulId').find(".page-link").click(function(e){
				var dataPage = $(this).attr('${P.dataPage}');
				Yet.pageN = parseInt(dataPage);
				Yet.listFilter();
				return false;
			});

			"""
		}

	}
}

fun Tag.paginationByRowCount(rowCount: Int) {
	val pc = (rowCount + P.pageSize - 1) / P.pageSize
	val n = this.httpContext.httpParams.int(P.pageN) ?: 0
	this.paginationBuild(pc, n)
	this.span {
		+"共 $rowCount 条记录"
	}
}


