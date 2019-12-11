package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*


fun Tag.tableNormal(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.table(class_ to _table, *kv, block = block)
}

fun Tag.tableDark(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.table(class_ to _table.._table_dark, *kv, block = block)
}

fun Tag.theadDark(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.thead(class_ to _thead_dark, *kv, block = block)
}

fun Tag.theadLight(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.thead(class_ to _thead_light, *kv, block = block)
}

fun Tag.tableResponsive(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _table_responsive, *kv, block = block)
}
