package dev.entao.kage.widget

import dev.entao.kage.Tag
import dev.entao.kage.TagCallback
import dev.entao.kage.div
import dev.entao.kage.scriptBlock
import dev.entao.kage.B
import dev.entao.kage.P

//window.yetQueryCond = q;
fun Tag.queryForm(block: TagCallback) {
	this.form {
		method = B.GET
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
		method = B.GET
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