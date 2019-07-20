package dev.entao.keb.page

import dev.entao.keb.core.HttpContext
import dev.entao.keb.page.html.*
import dev.entao.keb.page.widget.a
import dev.entao.keb.page.widget.button
import kotlin.math.max

class DialogBuild(context: HttpContext) {
	val modal = Tag(context, "div")

	var closeText: String = "关闭"

	var titleBlock: (Tag) -> Unit = { it.textEscaped("Title") }
	var bodyBlock: (Tag) -> Unit = { it.p { +"Body" } }

	var buttonsBlock: (Tag) -> Unit = {}

	fun title(titleText: String) {
		titleBlock = { it.textEscaped(titleText) }
	}


	fun build(): Tag {
		modal.apply {
			classList += "modal"
			tabindex = "-1"
			role = "dialog"
			div {
				classList += "modal-dialog"
				role = "document"
				div {
					classList += "modal-content"
					div {
						classList += "modal-header"
						h5 {
							classList += "modal-title"
							titleBlock(this)
						}
						button {
							classList += "close"
							dataDismiss = "modal"
							span {
								textUnsafe("&times;")
							}
						}
					}
					div {
						classList += "modal-body"
						//body here
						bodyBlock(this)
					}
					div {
						classList += "modal-footer"
						button {
							classList += B.btnSecondary
							dataDismiss = "modal"
							+closeText
						}
						buttonsBlock(this)
					}
				}
			}
		}
		return modal
	}
}

/**
 * Created by entaoyang@163.com on 2018/3/26.
 * parent.navbarDark {
 *      navbarBrand("brand")
 *      navbarCollapse {
 *          navbarNav {
 *              navbarItem {
 *                  classActive()
 *                  navbarItemLink("Home", "#")
 *              }
 *              navbarItem {
 *                  navbarItemLink("Shop", "#")
 *              }
 *              navbarItem {
 *                  navbarItemLink("About", "#")
 *              }
 *              navbarItemDropdown("Hello") {
 *                  dropdownItemLink("A", "#")
 *                  dropdownItemLink("B", "#")
 *                  dropdownItemLink("C", "#")
 *                  dropdownItemLink("D", "#")
 *              }
 *          }
 *      }
 * }
 */

fun Tag.navbarDark(brandText: String, brandLink: String = "#", block: TagCallback): Tag {
	return nav {
		clazz = "navbar navbar-expand-lg navbar-dark bg-dark"
		a {
			clazz = B.navbarBrand
			href = brandLink
			+brandText
		}
		val collId = eleId("collapse")
		button {
			clazz = B.navbarToggler
			dataToggle = B.collapse
			dataTarget = "#$collId"
			ariaControls = collId
			ariaExpanded = "false"
			ariaLabel = "Toggle Navigation"
			span {
				clazz = B.navbarTogglerIcon
			}
		}
		div {
			id = collId
			clazz = "collapse navbar-collapse"
			ul {
				clazz = "navbar-nav mr-auto"
				this.block()
			}
		}
	}
}




fun Tag.navbarItemLink(text: String, link: String, active: Boolean = false): Tag {
	return li {
		if (active) {
			clazz = "nav-item active"
		} else {
			clazz = "nav-item"
		}

		a {
			clazz = "nav-link"
			href = link
			+text
		}
	}
}


fun Tag.navbarText(block: TagCallback): Tag {
	return span {
		clazz = "navbar-text"
		this.block()
	}
}

fun Tag.navbarItemDropdown(text: String, block: TagCallback): Tag {
	return li {
		clazz = "nav-item dropdown"
		a {
			clazz = "nav-link dropdown-toggle"
			role = "button"
			dataToggle = "dropdown"
			href = "#"
			+text
		}
		div {
			clazz = "dropdown-menu"
			this.block()
		}
	}
}


fun Tag.dropdownItemLink(itemText: String, itemLink: String, active: Boolean): Tag {
	return a {
		if(active) {
			clazz = "dropdown-item active"
		}else {
			clazz = "dropdown-item"
		}
		href = itemLink
		+itemText
	}
}


fun Tag.pagination(block: TagCallback) {
	nav {
		ul {
			classList += "pagination"
			id = "pagination"
			this.block()
		}
	}
}

fun Tag.pageItem(block: TagCallback) {
	li {
		classList += "page-item"
		this.block()
	}
}

fun Tag.pageLink(block: TagCallback) {
	a {
		classList += "page-link"
		href = "#"
		this.block()
	}
}

fun Tag.pageItemLink(dataPageN: Int, block: TagCallback) {
	pageItem {
		pageLink {
			attr(P.dataPage, "$dataPageN")
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
				parentTag!!.classList += B.disabled
			}
			textUnsafe(S.firstPage)
		}
		pageItemLink(max(0, currentPage - 1)) {
			if (currentPage == 0) {
				parentTag!!.classList += B.disabled
			}
			textUnsafe(S.prePage)
		}

		val M = 14
		val M2 = M / 2
		if (pageCount <= M) {
			for (i in 0..(pageCount - 1)) {
				pageItemLink(i) {
					if (i == currentPage) {
						parentTag!!.classList += B.active
					}
					+"${i + 1}"
				}
			}
		} else {
			val from :Int
			val end :Int
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
						parentTag!!.classList += B.active
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
				parentTag!!.classList += B.disabled
			}
			textUnsafe(S.nextPage)
		}
		pageItemLink(pageCount - 1) {
			if (currentPage >= pageCount - 1) {
				parentTag!!.classList += B.disabled
			}
			textUnsafe(S.lastPage)
		}

		val ulId = id
		scriptBlock {
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


