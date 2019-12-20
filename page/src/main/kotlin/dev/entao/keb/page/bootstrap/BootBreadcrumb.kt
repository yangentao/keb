package dev.entao.keb.page.bootstrap

import dev.entao.keb.page.tag.*

//<nav aria-label="breadcrumb">
//	<ol class="breadcrumb">
//		<li class="breadcrumb-item"><a href="#">Home</a></li>
//		<li class="breadcrumb-item"><a href="#">Library</a></li>
//		<li class="breadcrumb-item active" aria-current="page">Data</li>
//	</ol>
//</nav>

// block 返回 active
fun Tag.breadcrumb(itemCount: Int, itemCallback: (Tag, Int) -> Boolean) {
	this.nav(aria_label_ to "breadcrumb") {
		ol(class_ to _breadcrumb) {
			for (i in 0 until itemCount) {
				li(class_ to _breadcrumb_item) {
					if (itemCallback(this, i)) {
						this += _active
					}
				}
			}
		}
	}
}