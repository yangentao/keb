package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*

//<div class="btn-group" role="group" aria-label="Basic example">
//	<button type="button" class="btn btn-secondary">Left</button>
//	<button type="button" class="btn btn-secondary">Middle</button>
//	<button type="button" class="btn btn-secondary">Right</button>
//</div>
fun Tag.buttonGroup(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(role_ to "group", class_ to _btn_group, *kv, block = block)
}

fun Tag.buttonGroup(theme: HClass, vararg kv: HKeyValue, block: TagCallback): Tag {
	val g = this.div(role_ to "group", class_ to _btn_group, *kv, block = block)
	g[tagname_ to "button"].forEach {
		it[class_] = _btn..theme
	}
	return g
}

//<div class="btn-toolbar" role="toolbar" aria-label="Toolbar with button groups">
//	<div class="btn-group mr-2" role="group" aria-label="First group">
//		<button type="button" class="btn btn-secondary">1</button>
//		<button type="button" class="btn btn-secondary">2</button>
//		<button type="button" class="btn btn-secondary">3</button>
//		<button type="button" class="btn btn-secondary">4</button>
//	</div>
//	<div class="btn-group mr-2" role="group" aria-label="Second group">
//		<button type="button" class="btn btn-secondary">5</button>
//		<button type="button" class="btn btn-secondary">6</button>
//		<button type="button" class="btn btn-secondary">7</button>
//	</div>
//	<div class="btn-group" role="group" aria-label="Third group">
//		<button type="button" class="btn btn-secondary">8</button>
//	</div>
//</div>
fun Tag.buttonToolbar(vararg kv: HKeyValue, block: TagCallback): Tag {
	return this.div(role_ to "toolbar", class_ to _btn_toolbar, *kv, block = block)
}

//<div class="btn-group" role="group">
//	<button id="btnGroupDrop1" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
//		Dropdown
//	</button>
//	<div class="dropdown-menu" aria-labelledby="btnGroupDrop1">
//		<a class="dropdown-item" href="#">Dropdown link</a>
//		<a class="dropdown-item" href="#">Dropdown link</a>
//	</div>
//</div>
fun Tag.buttonGroupDropdown(btnText: String, size: Int, block: (Tag, Int) -> Unit): Tag {
	return this.buttonGroup() {
		button(class_ to _btn_secondary.._dropdown_toggle) {
			+btnText
		}
		div(class_ to _dropdown_menu) {
			for (i in 0 until size) {
				a(class_ to _dropdown_item, href_ to "#") {
					block(this, i)
				}
			}
		}
	}
}