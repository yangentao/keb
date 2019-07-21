@file:Suppress("unused")

package dev.entao.keb.page.widget

import dev.entao.kava.base.userLabel
import dev.entao.keb.core.UriMake
import dev.entao.keb.page.B
import dev.entao.keb.page.P
import dev.entao.keb.page.S
import dev.entao.keb.page.eleId
import dev.entao.keb.page.html.*
import kotlin.math.max
import kotlin.reflect.KFunction

fun Tag.flex(block: Tag.() -> Unit): Tag {
	return this.div {
		clazz = B.flex
		this.block()
	}
}

fun Tag.flexRow(block: Tag.() -> Unit): Tag {
	return this.div {
		classList += B.flex
		classList += B.Flex.row
		this.block()
	}
}

fun Tag.progress(block: Tag.() -> Unit): Tag {
	return this.div {
		clazz = "progress"
		this.block()
	}
}

fun Tag.progressBar(block: Tag.() -> Unit): Tag {
	return this.div {
		clazz = "progress-bar"
		role = "progressbar"
		ariaValueMin = "0"
		ariaValueMax = "100"
		this.block()
	}
}

fun Tag.divContainer(block: TagCallback) {
	this.div {
		clazz = "container"
		this.block()
	}
}

fun Tag.divContainerFluid(block: TagCallback) {
	this.div {
		clazz = "container-fluid"
		this.block()
	}
}

fun Tag.divRow(block: TagCallback) {
	this.div {
		clazz = "row"
		this.block()
	}
}

fun Tag.divCol(block: TagCallback) {
	this.div {
		clazz = "col"
		this.block()
	}
}

fun Tag.divCol1(block: TagCallback) {
	this.div {
		clazz = "col-md-1"
		this.block()
	}
}

fun Tag.divCol2(block: TagCallback) {
	this.div {
		clazz = "col-md-2"
		this.block()
	}
}

fun Tag.divCol3(block: TagCallback) {
	this.div {
		clazz = "col-md-3"
		this.block()
	}
}

fun Tag.divCol4(block: TagCallback) {
	this.div {
		clazz = "col-md-4"
		this.block()
	}
}

fun Tag.divCol5(block: TagCallback) {
	this.div {
		clazz = "col-md-5"
		this.block()
	}
}

fun Tag.divCol6(block: TagCallback) {
	this.div {
		clazz = "col-md-6"
		this.block()
	}
}

fun Tag.divCol7(block: TagCallback) {
	this.div {
		clazz = "col-md-7"
		this.block()
	}
}

fun Tag.divCol8(block: TagCallback) {
	this.div {
		clazz = "col-md-8"
		this.block()
	}
}

fun Tag.divCol9(block: TagCallback) {
	this.div {
		clazz = "col-md-9"
		this.block()
	}
}

fun Tag.divCol10(block: TagCallback) {
	this.div {
		clazz = "col-md-10"
		this.block()
	}
}

fun Tag.divCol11(block: TagCallback) {
	this.div {
		clazz = "col-md-11"
		this.block()
	}
}

fun Tag.divCol12(block: TagCallback) {
	this.div {
		clazz = "col-md-12"
		this.block()
	}
}

fun Tag.offsetCol(n: Int) {
	if (n in 1..11) {
		classList += "offset-md-$n"
	}
}

fun Tag.navPills(block: TagCallback) {
	nav {
		clazz = "nav nav-pills"
		this.block()
	}
}

fun Tag.navLink(action: KFunction<*>, param: Any?) {
	val actionUri = UriMake(httpContext, action).param(param).uri
	a {
		addClass("nav-link")
		if (httpContext.currentUri in actionUri) {
			addClass("active")
		}
		href = actionUri
		+action.userLabel
	}
}

class CarouselItem {
	var toUrl: String = ""
	var imgSrc: String = ""
	var imgAlt: String = ""
	var caption: String = ""
	var text: String = ""
}

fun Tag.carousel(imgList: List<CarouselItem>, carouselId: String = "carouselId1") {
	div {
		this.id = carouselId
		clazz = "carousel slide"
		dataRide = "carousel"

		ol {
			clazz = "carousel-indicators"
			for (n in imgList.indices) {
				li {
					dataTarget = "#$carouselId"
					dataSlideTo = "$n"
					if (n == 0) {
						clazz = "active"
					}
				}
			}
		}
		div {
			clazz = "carousel-inner"
			imgList.forEachIndexed { n, item ->
				div {
					if (n == 0) {
						clazz = "carousel-item active"
					} else {
						clazz = "carousel-item"
					}
					var p = this
					if (!item.toUrl.isEmpty()) {
						p = a {
							href = item.toUrl

						}
					}
					p.img {
						clazz = "d-block w-100"
						src = item.imgSrc
						alt = item.imgAlt
					}
					if (item.caption.isNotEmpty() || item.text.isNotEmpty()) {
						div {
							clazz = "carousel-caption d-none d-md-block"
							if (item.caption.isNotEmpty()) {
								h5 {
									+item.caption
								}
								p {
									+item.text
								}
							}
						}
					}
				}
			}
		}
		a {
			clazz = "carousel-control-prev"
			href = "#$carouselId"
			role = "button"
			dataSlide = "prev"
			span {
				clazz = "carousel-control-prev-icon"
				ariaHidden = "true"
			}
			span {
				clazz = "sr-only"
				+"Previous"
			}
		}
		a {
			clazz = "carousel-control-next"
			href = "#$carouselId"
			role = "button"
			dataSlide = "next"
			span {
				clazz = "carousel-control-next-icon"
				ariaHidden = "true"
			}
			span {
				clazz = "sr-only"
				+"Next"
			}
		}

	}
}

fun Tag.breadcumb(items: List<LabelLink>) {
	breadcumb {
		items.forEachIndexed { n, item ->
			breadcrumbItem(n == items.lastIndex, item.label, item.link)
		}
	}
}

fun Tag.breadcumb(block: TagCallback): Tag {
	return this.tag("nav") {
		ol {
			clazz = "breadcrumb"
			this.block()
		}
	}
}

fun Tag.breadcrumbItem(active: Boolean, text: String, href: String) {
	this.li {
		if (active) {
			clazz = "breadcrumb-item active"
			ariaCurrent = "page"
			+text
		} else {
			clazz = "breadcrumb-item"
			a {
				this.href = href
				+text
			}
		}

	}
}

fun Tag.alertError(block: TagCallback): Tag {
	return this.alert(B.alertDanger, block)
}

fun Tag.alertSuccess(block: TagCallback): Tag {
	return this.alert(B.alertSuccess, block)
}

fun Tag.alertInfo(block: TagCallback): Tag {
	return this.alert(B.alertInfo, block)
}

fun Tag.alertWarning(block: TagCallback): Tag {
	return this.alert(B.alertWarning, block)
}

fun Tag.alert(theme: String, block: TagCallback): Tag {
	return div {
		clazz = "alert $theme alert-dismissible fade show"
		role = B.alert
		this.block()
		button {
			clazz = B.close
			dataDismiss = B.alert
			ariaLabel = "关闭"
			span {
				ariaHidden = "true"
				textUnsafe("&times;")
			}
		}
	}

}

fun Tag.badge(badgeTheme: String, block: TagCallback): Tag {
	return span {
		clazz = "badge $badgeTheme"
		this.block()
	}
}

fun Tag.badgePill(badgeTheme: String, block: TagCallback): Tag {
	return span {
		clazz = "badge badge-pill $badgeTheme"
		this.block()
	}
}

fun Tag.badgeLink(badgeTheme: String, block: TagCallback): Tag {
	return a {
		clazz = "badge $badgeTheme"
		this.block()
	}
}

fun Tag.badgeLinkPill(badgeTheme: String, block: TagCallback): Tag {
	return a {
		clazz = "badge badge-pill $badgeTheme"
		this.block()
	}
}

fun Tag.cardBodyTitle(titleText: String, block: TagCallback) {
	this.card {
		cardHeader(titleText)
		cardBody(block)
	}
}

fun Tag.cardDeck(block: TagCallback): Tag {
	return this.div {
		clazz = "card-deck"
		this.block()
	}
}

fun Tag.card(block: TagCallback): Tag {
	return this.div {
		clazz = "card"
		this.block()
	}
}

fun Tag.cardImgTop(block: TagCallback): Tag {
	return this.img {
		clazz = "card-img-top"
		this.block()
	}
}

fun Tag.cardImgBottom(block: TagCallback): Tag {
	return this.img {
		clazz = "card-img-bottom"
		this.block()
	}
}

fun Tag.cardImg(block: TagCallback): Tag {
	return this.img {
		clazz = "card-img"
		this.block()
	}
}

fun Tag.cardImgOverlay(block: TagCallback): Tag {
	return this.div {
		clazz = "card-img-overlay"
		this.block()
	}
}

fun Tag.cardHeader(text: String) {
	this.cardHeader {
		div {
			clazz = "align-items-center"
			h6 { +text }
		}
	}
}

fun Tag.cardHeader(text: String, rightBlock: Tag.() -> Unit) {
	this.cardHeader {
		div {
			clazz = "d-flex justify-content-between align-items-center"
			div {
				h6 { +text }
			}
			div {
				this.rightBlock()
				if (this.children.isEmpty()) {
					removeFromParent()
				}
			}

		}
	}
}

fun Tag.cardHeader(block: TagCallback): Tag {
	return this.div {
		clazz = "card-header"
		this.block()
	}
}

fun Tag.cardFooter(text: String) {
	this.cardFooter {
		addClass(B.textRight)
		+text
	}
}

fun Tag.cardFooter(block: TagCallback): Tag {
	return this.div {
		clazz = "card-footer text-muted"
		this.block()
	}
}

fun Tag.cardBody(block: TagCallback): Tag {
	return this.div {
		clazz = "card-body"
		this.block()
	}
}

fun Tag.cardBodyTitle(block: TagCallback): Tag {
	return this.h5 {
		clazz = "card-title"
		this.block()
	}
}

fun Tag.cardBodySubTitle(block: TagCallback): Tag {
	return this.h6 {
		clazz = "card-subtitle mb-2 text-muted"
		this.block()
	}
}

fun Tag.cardBodyText(block: TagCallback): Tag {
	return this.p {
		clazz = "card-text"
		this.block()
	}
}

fun Tag.cardLink(block: TagCallback): Tag {
	return this.a {
		clazz = "card-link"
		this.block()
	}
}

fun Tag.listGroupFlush(block: TagCallback): Tag {
	return ul {
		clazz = "list-group list-group-flush"
		this.block()
	}
}

fun Tag.listGroupItem(block: TagCallback): Tag {
	return li {
		clazz = "list-group-item"
		this.block()
	}
}

//<div class="btn-group" role="group" aria-label="Basic example">
//  <button type="button" class="btn btn-secondary">Left</button>
//  <button type="button" class="btn btn-secondary">Middle</button>
//  <button type="button" class="btn btn-secondary">Right</button>
//</div>
fun Tag.buttonGroup(block: TagCallback): Tag {
	val d = div {
		clazz = "btn-group"
		role = "group"
		this.block()
	}
	return d
}

fun Tag.buttonToolbar(block: TagCallback): Tag {
	val d = div {
		clazz = "btn-toolbar"
		role = "toolbar"
		this.block()
	}
	return d
}

//在这里设置radio的name
fun Tag.buttonGroupToggle(block: TagCallback): Tag {
	return div {
		clazz = "btn-group btn-group-toggle"
		dataToggle = "buttons"
		this.block()
		removeAttr("name")
	}
}

fun Tag.buttonGroupToggleItem(active: Boolean, text: String, btnTheme: String = B.btnSecondary): Tag {
	lateinit var ret: Tag
	if (active) {
		label {
			clazz = "btn $btnTheme active"
			ret = radio {
				checked = true
				this.name = parentTag?.parentTag?.name ?: ""
				autocomplete = "off"
				+text
			}
		}
	} else {
		label {
			clazz = "btn $btnTheme"
			ret = radio {
				this.name = parentTag?.parentTag?.name ?: ""
				autocomplete = "off"
				+text
			}
		}
	}
	return ret
}

fun Tag.dropdown(block: TagCallback) {
	val a = this.div("dropdown", block)
	val b = a.findChild { it.tagName == "button" && "dropdown-toggle" in it.classList } ?: return
	val m = a.findChild { it.tagName == "div" && "dropdown-menu" in it.classList } ?: return

	m.set("aria-labelledby", b.needId())

}

fun Tag.toggleButton(block: TagCallback) {
	button {
		classList += "dropdown-toggle"
		dataToggle = "dropdown"
		ariaHaspopup = "true"
		ariaExpanded = "false"
		needId()
		this.block()
	}
}

fun Tag.dropdownMenu(block: TagCallback) {
	div("dropdown-menu", block)
}

fun Tag.dropdownItemButton(block: TagCallback) {
	button {
		clazz = "dropdown-item"
		this.block()
	}
}

fun Tag.dropdownItemLink(block: TagCallback) {
	a {
		clazz = "dropdown-item"
		this.block()
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
		if (brandText.isNotEmpty()) {
			a {
				clazz = B.navbarBrand
				href = brandLink
				+brandText
			}
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
			this.block()
		}
	}
}

fun Tag.navbarLeft(block: TagCallback) {
	this.ul {
		clazz = "navbar-nav mr-auto"
		this.block()
	}
}

fun Tag.navbarRight(block: TagCallback) {
	this.ul {
		clazz = "navbar-nav"
		this.block()
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
		if (active) {
			clazz = "dropdown-item active"
		} else {
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


