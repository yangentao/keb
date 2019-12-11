@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.page.tag

import dev.entao.keb.core.HttpContext


class LabelLink(var label: String, var link: String)

class TextEscaped(httpContext: HttpContext, var text: String = "") : Tag(httpContext, "__textescaped__") {
	var forView = true
}

class TextUnsafe(httpContext: HttpContext, var text: String = "") : Tag(httpContext, "__textunsafe__") {

}
