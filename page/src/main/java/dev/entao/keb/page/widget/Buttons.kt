package dev.entao.keb.page.widget

import dev.entao.kava.base.firstParamName
import dev.entao.kava.base.userLabel
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.WebPath
import dev.entao.keb.page.*
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.html.TagCallback
import dev.entao.keb.page.html.scriptBlock
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2018/3/23.
 */

open class ATag(httpContext: dev.entao.keb.core.HttpContext) : Tag(httpContext, "a") {

}

class LinkButton(httpContext: dev.entao.keb.core.HttpContext) : ATag(httpContext), ButtonLike<LinkButton> {

	init {
		this.clazz = B.btn
		this.role = B.button
	}

	override val self = this

	override fun btnTheme(theme: String): LinkButton {
		this.setTagTheme(this, theme)
		return this
	}

	fun fromAction(action: HttpAction, block: WebPath.() -> Unit): LinkButton {
		val p = httpContext.path.action(action)
		p.block()
		this.href = p.uri
		this.textEscaped(action.userLabel)
		val cs = action.findAnnotation<FormConfirm>()?.value ?: ""
		if (cs.isNotEmpty()) {
			this.dataConfirm = cs
			this.onclick = "var s = $(this).attr('data-confirm');return !s || confirm(s);"
		}
		this.applyTheme(action, false)
		return this
	}
}

class ButtonTag(httpContext: dev.entao.keb.core.HttpContext) : Tag(httpContext, "button"), ButtonLike<ButtonTag> {

	init {
		this.clazz = B.btn
		this.type = "button"
	}

	override val self = this

	fun fromAction(action: HttpAction) {
		needId()
		this.applyTheme(action, true)
		+action.userLabel
		dataUrl = httpContext.path.action(action).uri
		dataParamName = action.firstParamName ?: "id"
		dataConfirm = action.findAnnotation<FormConfirm>()?.value ?: ""
	}

	fun typeButton(): ButtonTag {
		this.type = "button"
		return this
	}

	fun typeSubmit(): ButtonTag {
		this.type = "submit"
		return this
	}

	override fun btnTheme(theme: String): ButtonTag {
		this.setTagTheme(this, theme)
		return this
	}

	fun submitAsync(respJS: String = ""): ButtonTag {
		this.type = "submit"
		val formTag = this.findParent { it.tagName == "form" } ?: return this
		val formId = formTag.needId()
		scriptBlock {
			"""
			$('#$formId').submit(function(e){
				e.preventDefault();
				var fm = $('#$formId');
				var data = fm.serialize();
				$.post(fm.attr('action'), data, function(resp){
						$respJS;
				});
				return false ;
			})
			"""
		}
		return this
	}
}

fun Tag.a(block: ATag.() -> Unit): ATag {
	val a = ATag(this.httpContext)
	this.addTag(a)
	a.block()
	return a
}

fun Tag.a(text: String, href: String): Tag {
	return this.a {
		this.href = href
		+text
	}
}

fun Tag.a(text: String, webpath: WebPath): Tag {
	return this.a(text, webpath.uri)
}

fun Tag.submit(text: String = "提交"): ButtonTag {
	return this.submit { +text }
}

fun Tag.submit(block: TagCallback): ButtonTag {
	val b = ButtonTag(this.httpContext)
	b.typeSubmit()
	b.btnPrimary()
	addTag(b)
	b.block()
	return b
}

fun Tag.button(block: ButtonTag.() -> Unit): ButtonTag {
	val b = ButtonTag(this.httpContext)
	addTag(b)
	b.block()
	return b
}

fun Tag.linkButton(text: String, href: String): LinkButton {
	val b = LinkButton(this.httpContext)
	b.href = href
	b.textEscaped(text)
	b.apply {
		classList += "mx-1"
		val headTag = this.findParent { "card-header" in it.classList }
		if (headTag != null) {
			this.btnSmall()
		}
	}
	return b
}

//link 用于跳转
fun Tag.linkButton(action: HttpAction, block: WebPath.() -> Unit): LinkButton {
	val b = LinkButton(this.httpContext)
	addTag(b)
	b.fromAction(action, block)
	return b
}

fun Tag.linkButton(action: HttpAction, paramValue: Any?): LinkButton {
	val b = LinkButton(this.httpContext)
	addTag(b)
	b.fromAction(action) {
		this.param(paramValue)
	}
	return b
}

interface ButtonLike<T : Tag> {

	val self: T

	fun confirm(text: String): T {
		self.dataConfirm = text
		if (self.tagName == "a") {
			self.onclick = "return confirm('$text')"
		}
		return self
	}

	fun applyTheme(action: HttpAction, defaultPrimary: Boolean): T {
		val m = action.findAnnotation<ActionTheme>()
		if (m != null) {
			this.btnTheme(m.value)
		} else {
			if (action.findAnnotation<ActionDanger>() != null) {
				btnDanger()
			} else if (action.findAnnotation<ActionSafe>() != null) {
				btnSuccess()
			} else if (action.findAnnotation<ActionPrimary>() != null) {
				btnPrimary()
			} else {
				if (defaultPrimary) {
					btnPrimary()
				} else {
					btnLink()
				}
			}
		}
		return self
	}

	fun reloadPage() {
		self.onclick = "Yet.reqReload(this); return false ;"
	}

	fun openDialog() {
		self.onclick = "Yet.openDialogPanel(this); return false;"
	}

	fun btnTheme(theme: String): T {
		self.classList.removeAll(B.btnThemeList)
		self.addClass(theme)
		return self
	}

	fun btnSmall(): T {
		self.addClass(B.btnSmall)
		return self
	}

	fun btnLarge(): T {
		self.addClass(B.btnLarge)
		return self
	}

	fun setTagTheme(tag: Tag, theme: String) {
		tag.classList.removeAll(B.btnThemeList)
		tag.addClass(theme)
	}

	fun btnLink(): T {
		return this.btnTheme(B.btnLink)
	}

	fun btnDefault(): T {
		return this.btnTheme(B.btnDefault)
	}

	fun btnDanger(): T {
		return this.btnTheme(B.btnDanger)
	}

	fun btnSuccess(): T {
		return this.btnTheme(B.btnSuccess)
	}

	fun btnPrimary(): T {
		return this.btnTheme(B.btnPrimary)
	}

	fun btnSecondary(): T {
		return this.btnTheme(B.btnSecondary)
	}

	fun btnWarning(): T {
		return this.btnTheme(B.btnWarning)
	}

	fun btnInfo(): T {
		return this.btnTheme(B.btnInfo)
	}

	fun btnLight(): Tag {
		return this.btnTheme(B.btnLight)
	}

	fun btnDark(): T {
		return this.btnTheme(B.btnDark)
	}

	fun btnOutlineDefault(): T {
		return this.btnTheme(B.btnOutlineDefault)
	}

	fun btnOutlinePrimary(): T {
		return this.btnTheme(B.btnOutlinePrimary)
	}

	fun btnOutlineSecondary(): T {
		return this.btnTheme(B.btnOutlineSecondary)
	}

	fun btnOutlineSuccess(): T {
		return this.btnTheme(B.btnOutlineSuccess)
	}

	fun btnOutlineDanger(): T {
		return this.btnTheme(B.btnOutlineDanger)
	}

	fun btnOutlineWarning(): T {
		return this.btnTheme(B.btnOutlineWarning)
	}

	fun btnOutlineInfo(): T {
		return this.btnTheme(B.btnOutlineInfo)
	}

	fun btnOutlineLight(): T {
		return this.btnTheme(B.btnOutlineLight)
	}

	fun btnOutlineDark(): T {
		return this.btnTheme(B.btnOutlineDark)
	}

}

