package dev.entao.main

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup

class PersonGroup(context: HttpContext) : HttpGroup(context) {

	fun addAction() {
		this::addAction
		context.writeTextPlain("Add")
	}

	fun delAction() {
		context.writeTextPlain("Delete ")
	}
}