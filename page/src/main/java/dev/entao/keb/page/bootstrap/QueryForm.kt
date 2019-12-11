package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*

//window.yetQueryCond = q;
fun Tag.queryForm(block: TagCallback) {
	this.form(method_ to V.GET) {
		this.block()
		submitPrimary("查询")
	}
}

fun Tag.queryFormEmpty(block: TagCallback) {
	this.form(method_ to V.GET) {
		this.block()

	}
}