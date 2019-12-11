package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*

fun Tag.badge(vararg kv: HKeyValue, block: TagCallback) {
	this.span(class_ to _badge, * kv, block = block)
}

fun Tag.badge(theme: HClass, vararg kv: HKeyValue, block: TagCallback) {
	this.span(class_ to _badge..theme, * kv, block = block)
}

fun Tag.badgeInfo(block: TagCallback) {
	this.badge(_badge_info, block = block)
}

fun Tag.badgePrimary(block: TagCallback) {
	this.badge(_badge_primary, block = block)
}

fun Tag.badgeSecondary(block: TagCallback) {
	this.badge(_badge_secondary, block = block)
}

fun Tag.badgeSuccess(block: TagCallback) {
	this.badge(_badge_success, block = block)
}

fun Tag.badgeDark(block: TagCallback) {
	this.badge(_badge_dark, block = block)
}


fun Tag.badgePill(theme: HClass, vararg kv: HKeyValue, block: TagCallback) {
	this.span(class_ to _badge.._badge_pill..theme, * kv, block = block)
}


fun Tag.badgeLink(theme: HClass, vararg kv: HKeyValue, block: TagCallback) {
	this.a(class_ to _badge..theme, * kv, block = block)
}
