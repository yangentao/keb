@file:Suppress("unused")

package dev.entao.keb.page.widget

import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.html.TagCallback
import dev.entao.keb.page.html.div
import dev.entao.kava.base.*
import dev.entao.keb.core.UriMake
import dev.entao.keb.page.html.rootTag
import kotlin.reflect.KFunction

/**
 * Created by entaoyang@163.com on 2018/3/21.
 */

fun Tag.methodGet() {
	method = "GET"
}

fun Tag.methodPost() {
	method = "POST"
}

fun Tag.enctypeMultipart() {
	this.enctype = "multipart/form-data"
}

fun Tag.form(formAction: KFunction<*>, block: TagCallback): Tag {
	return form {
		action = httpContext.actionUri(formAction)
		this.block()
	}
}

fun Tag.form(formAction: UriMake, block: TagCallback): Tag {
	return form {
		action = formAction.uri
		this.block()
	}
}

fun Tag.form(block: TagCallback): Tag {
	return tag("form") {
		methodPost()
		block()
		val fileTag = findChildDeep {
			it.tagName == "input" && it.type == "file"
		}
		if (fileTag != null) {
			enctypeMultipart()
			this.rootTag.needUploads = true
		}
	}
}

fun Tag.submitRow(text: String = "提交") {
	formGroupRow {
		div {
			submit(text)
		}
	}
}

fun Tag.feedbackValid(text: String) {
	div {
		clazz = "valid-feedback"
		+text
	}
}

fun Tag.feedbackInvalid(text: String) {
	div {
		clazz = "invalid-feedback"
		+text
	}
}



fun Tag.input(block: TagCallback): Tag {
	val t = tag("input")
	t.block()
	t.needId()
	return t
}


fun Tag.hidden(p: Prop0) {
	this.hidden {
		name = p.userName
		value = p.getValue()?.toString() ?: ""
	}
}

fun Tag.hidden(hiddenName: String, hiddenValue: Any?) {
	this.hidden {
		name = hiddenName
		value = hiddenValue?.toString() ?: ""
	}
}

fun Tag.hidden(p: Prop1, v: Any?) {
	this.hidden {
		name = p.userName
		value = v?.toString() ?: ""
	}
}

fun Tag.hidden(block: TagCallback): Tag {
	return input {
		type = "hidden"
		this.block()
	}
}

fun Tag.radio(block: TagCallback): Tag {
	return input {
		type = "radio"
		this.block()
	}
}

fun Tag.checkbox(block: TagCallback): Tag {
	return input {
		type = "checkbox"
		this.block()
	}
}

fun Tag.password(block: TagCallback): Tag {
	return input {
		type = "password"
		this.block()
	}
}

fun Tag.email(block: TagCallback): Tag {
	return input {
		type = "email"
		this.block()
	}
}

fun Tag.urlInput(block: TagCallback): Tag {
	return input {
		type = "url"
		this.block()
	}
}

fun Tag.number(block: TagCallback): Tag {
	return input {
		type = "number"
		this.block()
	}
}

fun Tag.dateInput(block: TagCallback): Tag {
	return input {
		type = "date"
		this.block()
	}
}

fun Tag.timeInput(block: TagCallback): Tag {
	return input {
		type = "time"
		this.block()
	}
}

fun Tag.dateTimeInput(block: TagCallback): Tag {
	return input {
		type = "datetime"
		this.block()
	}
}

fun Tag.fileInput(block: TagCallback): Tag {
	return input {
		type = "file"
		this.block()
	}
}

fun Tag.typeFile() {
	type = "file"
}

fun Tag.typeDateTime() {
	type = "datetime"
}

fun Tag.typeTime() {
	type = "time"
}

fun Tag.typeDate() {
	type = "date"
}

fun Tag.typeNumber() {
	type = "number"
}

fun Tag.typeUrl() {
	type = "url"
}

fun Tag.typeEmail() {
	type = "email"
}

fun Tag.typePassword() {
	type = "password"
}

fun Tag.typeCheckbox() {
	type = "checkbox"
}

fun Tag.typeRadio() {
	type = "radio"
}

fun Tag.typeHidden() {
	type = "hidden"
}

fun Tag.typeText() {
	type = "text"
}

fun Tag.formTextMuted(block: TagCallback): Tag {
	val t = tag("small")
	t.clazz = "form-text text-muted"
	t.block()
	return t
}

fun Tag.helpText(text: String) {
	this.formTextMuted(text)
}

fun Tag.formTextMuted(text: String) {
	formTextMuted {
		+text
	}
}

fun Tag.patternDouble() {
	pattern = """[0-9]+([\.][0-9]+)?"""
}

fun Tag.patternNumber() {
	pattern = """[+-]?[0-9]+([\.][0-9]+)?"""
}

fun Tag.patternInteger() {
	pattern = """[-]?[0-9]+"""
}

fun Tag.patternPhone11() {
	pattern = """1[0-9]{10}"""
}

fun Tag.patternTel() {
	pattern = """[0-9]{3,12}"""
}