@file:Suppress("unused")

package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*


val _colx = _col_12.._col_md
val _colx_1 = _col_12.._col_md_1
val _colx_2 = _col_12.._col_md_2
val _colx_3 = _col_12.._col_md_3
val _colx_4 = _col_12.._col_md_4
val _colx_5 = _col_12.._col_md_5
val _colx_6 = _col_12.._col_md_6
val _colx_7 = _col_12.._col_md_7
val _colx_8 = _col_12.._col_md_8
val _colx_9 = _col_12.._col_md_9
val _colx_10 = _col_12.._col_md_10
val _colx_11 = _col_12.._col_md_11
val _colx_12 = _col_12.._col_md_12

fun Tag.divContainer(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _container, *kv, block = block)
}

fun Tag.divContainerFluid(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _container_fluid, *kv, block = block)
}

fun Tag.divRow(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _row, * kv, block = block)
}

fun Tag.divCol(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col, * kv, block = block)
}


fun Tag.divCol1(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_1, *kv, block = block)
}

fun Tag.divCol2(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_2, *kv, block = block)
}

fun Tag.divCol3(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_3, *kv, block = block)
}

fun Tag.divCol4(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_4, *kv, block = block)
}

fun Tag.divCol5(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_5, *kv, block = block)
}

fun Tag.divCol6(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_6, *kv, block = block)
}

fun Tag.divCol7(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_7, *kv, block = block)
}

fun Tag.divCol8(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_8, *kv, block = block)
}

fun Tag.divCol9(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_9, *kv, block = block)
}

fun Tag.divCol10(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_10, *kv, block = block)
}

fun Tag.divCol11(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_11, *kv, block = block)
}

fun Tag.divCol12(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _col_12, *kv, block = block)
}


fun Tag.row(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _row, * kv, block = block)
}

fun Tag.rowCenter(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _row.._justify_content_center, * kv, block = block)
}

fun Tag.colX(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx, *kv, block = block)
}

fun Tag.colXN(n: Int, vararg kv: HKeyValue, block: TagCallback): Tag {
	assert(n in 1..12)
	return this.div(class_ to _col_12.."col-md-$n", *kv, block = block)
}

fun Tag.colX1(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_1, *kv, block = block)
}

fun Tag.colX2(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_2, *kv, block = block)
}

fun Tag.colX3(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_3, *kv, block = block)
}

fun Tag.colX4(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_4, *kv, block = block)
}

fun Tag.colX5(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_5, *kv, block = block)
}

fun Tag.colX6(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_6, *kv, block = block)
}


fun Tag.colX7(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_7, *kv, block = block)
}

fun Tag.colX8(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_8, *kv, block = block)
}

fun Tag.colX9(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_9, *kv, block = block)
}

fun Tag.colX10(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_10, *kv, block = block)
}

fun Tag.colX11(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_11, *kv, block = block)
}

fun Tag.colX12(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(class_ to _colx_12, *kv, block = block)
}