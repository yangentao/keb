package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*


class CarouselItem {
	var imgSrc: String = ""
	var title: String = ""
	var desc: String = ""
	var linkTo: String = ""
}

fun Tag.buildCarousel(ident: String, items: List<CarouselItem>): Tag {
	return div(id_ to ident, class_ to _carousel.."slide", data_ride_ to _carousel) {
		ol(class_ to _carousel_indicators) {
			for (i in items.indices) {
				val t = li(data_target_ to "#$ident", data_slide_to_ to "$i")
				if (i == 0) {
					t += _active
				}
			}
		}
		div(class_ to _carousel_inner) {
			for (i in items.indices) {
				val item = items[i]
				val t = div(class_ to _carousel_item) {
					if (item.linkTo.isEmpty()) {
						img(class_ to _d_block.._w_100, src_ to item.imgSrc)
					} else {
						a(href_ to item.linkTo) {
							img(class_ to _d_block.._w_100, src_ to item.imgSrc)
						}
					}
					div(class_ to _carousel_caption.._d_none.._d_md_block) {
						h5 { +item.title }
						p { +item.desc }
					}
				}
				if (i == 0) {
					t += _active
				}
			}
		}
		a(class_ to _carousel_control_prev, href_ to "#$ident", role_ to "button", data_slide_ to "prev") {
			span(class_ to _carousel_control_prev_icon, aria_hidden_ to "true")
			span(class_ to _sr_only) { +"Previous" }
		}
		a(class_ to _carousel_control_next, href_ to "#$ident", role_ to "button", data_slide_ to "next") {
			span(class_ to _carousel_control_next_icon, aria_hidden_ to "true")
			span(class_ to _sr_only) { +"Previous" }
		}
	}
}