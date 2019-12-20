package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*

fun Tag.imgFluid(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("img", class_ to _img_fluid, *kv, block = block)
}

fun Tag.imgThumbnail(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("img", class_ to _img_thumbnail, *kv, block = block)
}

fun Tag.figure(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("figure", class_ to _figure, *kv, block = block)
}

fun Tag.figureCaption(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("figcaption", class_ to _figure_caption, *kv, block = block)
}

fun Tag.figureImage(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.tag("img", class_ to _figure_img.._img_fluid.._rounded, *kv, block = block)
}