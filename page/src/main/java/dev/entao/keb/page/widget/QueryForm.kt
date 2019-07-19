package dev.entao.keb.page.widget

import dev.entao.keb.page.*
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.html.TagCallback
import dev.entao.keb.page.html.div
import dev.entao.keb.page.html.scriptBlock

//window.yetQueryCond = q;
fun Tag.queryForm(block: TagCallback) {
	this.form {
		method = dev.entao.keb.page.B.GET
		id = P.QUERY_FORM
		this.block()
		val formId = this.id
		formGroupRow {
			div {
				submit {
					+"查询"
				}
			}
		}
		scriptBlock {
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
	this.form {
		method = dev.entao.keb.page.B.GET
		id = P.QUERY_FORM
		this.block()
		val formId = this.id

		scriptBlock {
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