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

