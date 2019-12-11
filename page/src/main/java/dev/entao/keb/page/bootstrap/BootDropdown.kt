package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*

fun Tag.dropdownDiv(vararg kv: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _dropdown, *kv, block = block)
}

fun Tag.dropdownToggleButton(vararg kv: HKeyValue, block: TagCallback): Tag {
	return buttonB(class_ to _dropdown_toggle,
			data_toggle_ to _dropdown, aria_haspopup_ to "true", aria_expanded_ to "false",
			*kv, block = block)

}


fun Tag.dropdownMenu(vararg kv: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _dropdown_menu, *kv, block = block)
}

fun Tag.dropdownMenuRight(vararg kv: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _dropdown_menu.._dropdown_menu_right, *kv, block = block)
}

fun Tag.dropdownItem(vararg kv: HKeyValue, block: TagCallback): Tag {
	return a(class_ to _dropdown_item, *kv, block = block)
}