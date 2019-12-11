package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*

//<div class="card" style="width: 18rem;">
//	<img src="..." class="card-img-top" alt="...">
//	<div class="card-body">
//		<h5 class="card-title">Card title</h5>
//		<p class="card-text">Some quick example text to build on the card title and make up the bulk of the card's content.</p>
//		<a href="#" class="btn btn-primary">Go somewhere</a>
//	</div>
//</div>

fun Tag.cardBodyTitle(titleText: String, block: TagCallback) {
	this.card {
		cardHeaderH6 { +titleText }
		cardBody(block = block)
	}
}


fun Tag.card(vararg vs: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _card, *vs, block = block)
}

fun Tag.cardHeader(vararg vs: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _card_header, *vs, block = block)
}

fun Tag.cardHeaderH6(vararg vs: HKeyValue, block: TagCallback): Tag {
	return h6(class_ to _card_header, *vs, block = block)
}

fun Tag.cardBody(vararg vs: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _card_body, *vs, block = block)
}

fun Tag.cardFooter(vararg vs: HKeyValue, block: TagCallback): Tag {
	return div(class_ to _card_footer, *vs, block = block)
}

fun Tag.cardTitle(vararg vs: HKeyValue, block: TagCallback): Tag {
	return h5(class_ to _card_title, *vs, block = block)
}

fun Tag.cardSubTitle(vararg vs: HKeyValue, block: TagCallback): Tag {
	return h6(class_ to _card_title.._text_muted.._mb_2, *vs, block = block)
}

fun Tag.cardText(vararg vs: HKeyValue, block: TagCallback): Tag {
	return p(class_ to _card_text, *vs, block = block)
}

fun Tag.cardImgTop(vararg vs: HKeyValue, block: TagCallback): Tag {
	return img(class_ to _card_img_top, *vs, block = block)
}

fun Tag.cardLink(vararg vs: HKeyValue, block: TagCallback): Tag {
	return a(class_ to _card_link, href_ to "#", *vs, block = block)
}