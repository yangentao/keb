package dev.entao.keb.page.widget

import dev.entao.keb.page.P
import dev.entao.keb.page.tag.Tag
import dev.entao.keb.page.tag.TagCallback
import dev.entao.keb.page.tag.div
import dev.entao.keb.page.tag.scriptBlock

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