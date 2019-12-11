package dev.entao.keb.page.bootstrap

import dev.entao.kava.base.userLabel
import dev.entao.keb.core.ActionURL
import dev.entao.keb.core.HttpAction
import dev.entao.keb.page.tag.*


fun Tag.navItem(action: HttpAction, label: String = "") {
	navItem(ActionURL(action), label)
}

fun Tag.navItem(action: ActionURL, label: String = "") {
	val acUri = this.httpContext.actionUri(action.action)
	li(class_ to _nav_item) {
		a(class_ to _nav_link) {
			if (label.isNotEmpty()) {
				+label
			} else {
				+action.action.userLabel
			}
			this[href_] = action.toURL(httpContext)
		}
		if (httpContext.currentUri == acUri) {
			var active = true
			for ((k, v) in action.map) {
				active = active && (httpContext.params.str(k) == v)
			}
			if (active) {
				this += _active
			}
		}
	}
}


fun Tag.navbar(clazz: String, block: TagCallback) {
	nav(class_ to _navbar.._navbar_expand_lg..clazz, block = block)
}

fun Tag.navbarBrand(vararg kv: HKeyValue, block: TagCallback) {
	a(class_ to _navbar_brand, href_ to "#", *kv, block = block)
}

fun Tag.navbarToggler(targetId: String) {
	button(class_ to _navbar_toggler, data_toggle_ to "collapse",
			data_target_ to "#$targetId",
			aria_controls_ to targetId,
			aria_expanded_ to "false") {
		span(class_ to _navbar_toggler_icon) {}
	}
}

fun Tag.navbarCollapse(id: String, count: Int, itemCallback: (Tag, Int) -> Boolean) {
	div(class_ to _collapse.._navbar_collapse, id_ to id) {
		ul(class_ to _navbar_nav.._mr_auto) {
			for (i in 0 until count) {
				li(class_ to _nav_item) {
					val aLink = a(class_ to _nav_link, href_ to "#")
					if (itemCallback(aLink, i)) {
						this += _active
					}
				}
			}
		}
	}
}
//
//<nav class="navbar navbar-expand-lg navbar-light bg-light">
//	<a class="navbar-brand" href="#">Navbar</a>
//	<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
//		<span class="navbar-toggler-icon"></span>
//	</button>
//
//	<div class="collapse navbar-collapse" id="navbarSupportedContent">
//		<ul class="navbar-nav mr-auto">
//			<li class="nav-item">
//				<a class="nav-link" href="#">Link</a>
//			</li>
//			<li class="nav-item dropdown">
//				<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
//					Dropdown
//				</a>
//				<div class="dropdown-menu" aria-labelledby="navbarDropdown">
//					<a class="dropdown-item" href="#">Action</a>
//					<a class="dropdown-item" href="#">Another action</a>
//					<div class="dropdown-divider"></div>
//					<a class="dropdown-item" href="#">Something else here</a>
//				</div>
//			</li>
//			<li class="nav-item">
//				<a class="nav-link disabled" href="#" tabindex="-1" aria-disabled="true">Disabled</a>
//			</li>
//		</ul>
//		<form class="form-inline my-2 my-lg-0">
//			<input class="form-control mr-sm-2" type="search" placeholder="Search" aria-label="Search">
//			<button class="btn btn-outline-success my-2 my-sm-0" type="submit">Search</button>
//		</form>
//	</div>
//</nav>