@file:Suppress("unused")

package dev.entao.keb.biz

import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.card
import dev.entao.keb.page.cardBody
import dev.entao.keb.page.cardHeader
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.sidebarPage

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
