package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*


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

