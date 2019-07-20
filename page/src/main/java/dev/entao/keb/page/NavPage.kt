@file:Suppress("unused")

package dev.entao.keb.page

import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.html.Tag

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
