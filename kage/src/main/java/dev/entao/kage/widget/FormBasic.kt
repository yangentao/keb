@file:Suppress("unused")

package dev.entao.kage.widget

import dev.entao.kage.Tag
import dev.entao.kava.base.userName
import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop0
import dev.entao.kava.base.Prop1
import dev.entao.ken.WebPath
import dev.entao.kava.base.getValue
import dev.entao.kage.TagCallback
import dev.entao.kage.div
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

fun Tag.form(formAction: KFunction<Unit>, block: TagCallback): Tag {
	return form {
		action = formAction.path.uri
		this.block()
	}
}

fun Tag.form(formAction: WebPath, block: TagCallback): Tag {
	return form {
		action = formAction.uri
		this.block()
	}
}

fun Tag.form(block: TagCallback): Tag {
	return addTag("form") {
		methodPost()
		block()
		val fileTag = findChildDeep {
			it.tagName == "input" && it.type == "file"
		}
		if (fileTag != null) {
			enctypeMultipart()
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

fun Tag.formCheck(block: TagCallback): Tag {
	return div {
		clazz = "form-check"
		this.block()
		val lb = this.findChild { it.tagName == "label" }
		val cb = this.children.find { it.tagName == "input" && (it.type == "checkbox" || it.type == "radio") }
		cb?.addClassFirst("form-check-input")
		lb?.addClassFirst("form-check-label")
		lb?.needFor(cb)
	}
}

fun Tag.validFeedback(text: String) {
	div {
		clazz = "valid-feedback"
		+text
	}
}

fun Tag.invalidFeedback(text: String) {
	div {
		clazz = "invalid-feedback"
		+text
	}
}

fun Tag.textarea(block: TagCallback): Tag {
	val t = addTag("textarea")
	t.rows = "3"
	t.block()
	t.needId()
	return t
}

fun Tag.input(block: TagCallback): Tag {
	val t = addTag("input")
	t.block()
	t.needId()
	return t
}

fun Tag.edit(p: Prop): Tag {
	return this.edit {
		name = p.userName
		if (p is Prop0) {
			value = p.getValue()?.toString() ?: ""
		}
	}
}

fun Tag.edit(block: TagCallback): Tag {
	return input {
		type = "text"
		this.block()
	}
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
	val t = addTag("small")
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