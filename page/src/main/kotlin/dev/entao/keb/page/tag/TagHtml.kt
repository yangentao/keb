@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.keb.page.tag

import dev.entao.keb.core.ActionURL
import kotlin.reflect.KFunction


fun Tag.form(formAction: KFunction<*>, vararg kv: HKeyValue, block: TagCallback): Tag {
	return form(*kv) {
		this += formAction
		this.block()
	}
}

fun Tag.form(formAction: ActionURL, vararg kv: HKeyValue, block: TagCallback): Tag {
	return form(*kv) {
		this += formAction
		this.block()
	}
}

fun Tag.formGET(formAction: KFunction<*>, vararg kv: HKeyValue, block: TagCallback): Tag {
	return form(method_ to V.GET, *kv) {
		this += formAction
		this.block()
	}
}

fun Tag.formGET(formAction: ActionURL, vararg kv: HKeyValue, block: TagCallback): Tag {
	return form(method_ to V.GET, *kv) {
		this += formAction
		this.block()
	}
}

fun Tag.formPOST(formAction: KFunction<*>, vararg kv: HKeyValue, block: TagCallback): Tag {
	return form(method_ to V.POST, *kv) {
		this += formAction
		this.block()
	}
}

fun Tag.formPOST(formAction: ActionURL, vararg kv: HKeyValue, block: TagCallback): Tag {
	return form(method_ to V.POST, *kv) {
		this += formAction
		this.block()
	}
}
