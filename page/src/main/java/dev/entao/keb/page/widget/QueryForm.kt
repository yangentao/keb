package dev.entao.keb.page.widget

import dev.entao.keb.page.P
import dev.entao.keb.page.bootstrap.submitPrimary
import dev.entao.keb.page.tag.*

//window.yetQueryCond = q;
fun Tag.queryForm(block: TagCallback) {
	this.form(id_ to P.QUERY_FORM, method_ to V.GET) {
		this.block()
		val formId = this[id_]
		submitPrimary("查询")
		script {
			"""
			Yet.queryFormId = '$formId';

			$('#$formId').submit(function(e){
				e.preventDefault();
				Yet.listFilter();
			})
			"""
		}
	}
}

fun Tag.queryFormEmpty(block: TagCallback) {
	this.form(id_ to P.QUERY_FORM, method_ to V.GET) {
		this.block()
		val formId = this[id_]

		script {
			"""
			Yet.queryFormId = '$formId';

			$('#$formId').submit(function(e){
				e.preventDefault();
				Yet.listFilter();
			})
			"""
		}
	}
}