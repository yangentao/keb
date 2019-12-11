package dev.entao.keb.page.groups

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpScope
import dev.entao.keb.page.R
import dev.entao.keb.page.tag.*

open class HtmlPage(final override val context: HttpContext) : HttpScope {
	val html = Tag(context, "html")

	init {
		html.tag("head") {}
		html.tag("body") {}
	}

	val head: Tag get() = html.head
	val body: Tag get() = html.body

	fun head(block: TagCallback) {
		this.head.block()
	}

	fun body(block: TagCallback) {
		this.body.block()
	}

	fun title(s: String) {
		val a = head.single("title")
		a.children.clear()
		a.textEscaped(s)
	}

	fun send() {
		context.sendHtmlTag(html)
	}

	fun setupBootstrap() {
		this.head {
			meta(charset_ to V.utf8)
			meta(http_equiv_ to V.X_UA_Compatible, content_ to V.IE_edge)
			meta(name_ to V.viewport, content_ to V.deviceWidth)

			link(rel_ to "stylesheet", href_ to context.resUri(R.awesomeCSS))
			link(rel_ to "stylesheet", href_ to context.resUri(R.bootCSS))

		}
		this.body {
			script(httpContext.resUri(R.jquery))
			script(httpContext.resUri(R.popperJS))
			script(httpContext.resUri(R.bootJS))
			script(httpContext.resUri(R.buttonsJS))

		}
	}

	fun setupMyCssJs() {
		this.head.link(rel_ to "stylesheet", href_ to context.resUri(R.myCSS))
		this.body.script(context.resUri(R.myJS))
	}
}

val Tag.head: Tag get() = this.root.single("head")

val Tag.body: Tag get() = this.root.single("body")

