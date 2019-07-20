package dev.entao.keb.page.html

import dev.entao.keb.core.HttpContext
import dev.entao.kava.base.*
import dev.entao.keb.page.*
import dev.entao.keb.page.ex.HtmlTemplate
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

open class Tag(val httpContext: HttpContext, var tagName: String) : HtmlTemplate {
	private val attrs: TagMap = TagMap()
	var parentTag: Tag? = null

	var outputScript = tagName == "body"

	val children = ArrayList<Tag>(32)
	val classList: ArrayList<String> = ArrayList()

	var id: String by attrs

	var name: String by attrs
	var clazz: String
		get() {
			return classList.joinToString(" ")
		}
		set(value) {
			classList.clear()
			val ls = value.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
			for (s in ls) {
				classList.add(s)
			}
		}
	var type: String by attrs
	var style: String by attrs
	var method: String by attrs
	var action: String by attrs
	var enctype: String by attrs
	var target: String by attrs
	var content: String by attrs
	var charset: String by attrs
	var lang: String by attrs
	var viewport: String by attrs
	@Name("http-equiv")
	var httpEquiv: String by attrs

	var tabindex: String by attrs
	var width: String by attrs
	var height: String by attrs
	var rows: String by attrs
	var cols: String by attrs
	var placeholder: String by attrs
	var max: String by attrs
	var min: String by attrs
	var maxlength: String by attrs
	var pattern: String by attrs
	var step: String by attrs
	var accept: String by attrs
	var scope: String by attrs

	var size: Int by attrs
	var color: String by attrs

	var list: String by attrs
	var label: String by attrs
	var value: String by attrs
	var role: String by attrs
	var align: String by attrs
	var src: String by attrs
	@Name("data-src")
	var dataSrc: String by attrs
	var alt: String by attrs
	var rel: String by attrs
	var href: String by attrs
	var onclick: String by attrs
	@Name("for")
	var forId: String by attrs

	var disabled: Boolean by attrs
	var readonly: Boolean by attrs
	var checked: Boolean by attrs
	var required: Boolean by attrs
	var selected: Boolean by attrs
	var multiple: Boolean by attrs

	@Name("aria-label")
	var ariaLabel: String by attrs

	@Name("aria-hidden")
	var ariaHidden: String by attrs
	@Name("aria-current")
	var ariaCurrent: String by attrs
	@Name("aria-controls")
	var ariaControls: String by attrs
	@Name("aria-expanded")
	var ariaExpanded: String by attrs
	@Name("aria-haspopup")
	var ariaHaspopup: String by attrs

	@Name("data-dismiss")
	var dataDismiss: String by attrs
	@Name("data-toggle")
	var dataToggle: String by attrs
	@Name("data-ride")
	var dataRide: String by attrs
	@Name("data-target")
	var dataTarget: String by attrs
	@Name("data-slide")
	var dataSlide: String by attrs
	@Name("data-slide-to")
	var dataSlideTo: String by attrs

	@Name("data-confirm")
	var dataConfirm: String by attrs

	@Name("data-url")
	var dataUrl: String by attrs

	@Name("data-param-name")
	var dataParamName: String by attrs

	@Name("aria-valuemin")
	var ariaValueMin: String by attrs

	@Name("aria-valuemax")
	var ariaValueMax: String by attrs

	var autocomplete: String by attrs
	var onchange: String by attrs

	@Name("data-select-value")
	var dataSelectValue: String by attrs

	init {
		if (tagName == "script") {
			this.type = "text/javascript"
		}
	}

	val htmlTag: Tag?
		get() {
			return this.findParent {
				it.tagName == "html"
			}
		}

	fun idName(idname: String) {
		id = idname
		name = idname
	}

	fun needFor(controlTag: Tag?) {
		if (controlTag != null && this.forId.isEmpty()) {
			this.forId = controlTag.needId()
		}
	}

	fun needId(): String {
		if (this.id.isEmpty()) {
			genId()
		}
		return this.id
	}

	fun genId() {
		++eleId
		if (this.type.isEmpty()) {
			this.id = tagName + "_$eleId"
		} else {
			this.id = tagName + "_" + this.type + "_$eleId"
		}
	}

	fun valueOf(p: KProperty<*>): Tag {
		value = p.strValue ?: ""
		return this
	}

	operator fun get(key: String): String {
		return attrs[key] ?: ""
	}

	operator fun set(key: String, value: String) {
		attr(key, value)
	}

	fun attr(pair: Pair<String, String>) {
		attr(pair.first, pair.second)
	}

	fun attr(key: String, value: String) {
		attrs.put(key, value)
	}

	fun removeAttr(key: String) {
		attrs.remove(key)
	}

	val noClass: Boolean get() = classList.isEmpty()

	fun addClassFirst(cls: String) {
		val ls: List<String> = cls.trim().splitToSequence(' ').map { it.trim() }.filter { it.notEmpty() }.toList().reversed()
		for (s in ls) {
			classList.remove(s)
			classList.add(0, s)
		}
	}

	fun addClass(vararg classes: String) {
		val ls = classes.flatMap { s -> s.trim().splitToSequence(' ').map { it.trim() }.filter { it.notEmpty() }.toList() }
		for (s in ls) {
			if (s !in classList) {
				classList += s
			}
		}
	}

	fun removeClass(vararg classes: String) {
		val ls = classes.flatMap { s -> s.trim().splitToSequence(' ').map { it.trim() }.filter { it.notEmpty() }.toList() }
		this.classList.removeAll(ls)
	}

	fun replaceClass(clsDel: String, clsAdd: String) {
		for (i in classList.indices) {
			if (classList[i] == clsDel) {
				classList[i] = clsAdd
				return
			}
		}
		classList.add(clsAdd)
	}

	fun removeFromParent() {
		parentTag?.children?.remove(this)
	}

	fun bringToFirst() {
		val ls = parentTag?.children ?: return
		ls.remove(this)
		ls.add(0, this)
	}

	fun filterDeep(block: (Tag) -> Boolean): ArrayList<Tag> {
		val ls = ArrayList<Tag>()
		this.filterDeep(ls, block)
		return ls
	}

	private fun filterDeep(ls: ArrayList<Tag>, block: (Tag) -> Boolean) {
		for (c in this.children) {
			if (block(c)) {
				ls += c
			}
			c.filterDeep(ls, block)
		}
	}

	fun findChildDeep(acceptor: (Tag) -> Boolean): Tag? {
		val t = children.find(acceptor)
		if (t != null) {
			return t
		}
		children.forEach {
			val tt = it.findChildDeep(acceptor)
			if (tt != null) {
				return tt
			}
		}
		return null
	}

	fun findChild(block: (Tag) -> Boolean): Tag? {
		return children.find(block)
	}

	fun findParent(block: (Tag) -> Boolean): Tag? {
		val p = this.parentTag ?: return null
		if (block(p)) {
			return p
		}
		return p.findParent(block)
	}

	fun removeTag(tagname: String) {
		children.removeIf { it.tagName == tagname }
	}

	fun addTag(tagname: String): Tag {
		val t = Tag(httpContext, tagname)
		addTag(t)
		return t
	}

	fun addTag(tagname: String, block: Tag.() -> Unit): Tag {
		val t = addTag(tagname)
		t.block()
		return t
	}

	fun addTag(tag: Tag): Tag {
		tag.parentTag = this
		children.add(tag)
		return tag
	}

	//==addText
	operator fun String?.unaryPlus() {
		textEscaped(this)
	}

	fun textUnsafe(block: () -> String?) {
		textUnsafe(block())
	}

	fun textUnsafe(text: String?) {
		addTag(TextUnsafe(httpContext, text ?: ""))
	}

	fun textEscaped(block: () -> String?): TextEscaped {
		return textEscaped(block())
	}

	fun textEscaped(text: String?): TextEscaped {
		val c = TextEscaped(httpContext, text ?: "")
		addTag(c)
		return c
	}


	private fun writeChildren2(singleLine: Boolean, buf: Appendable, level: Int) {
		val ls = children.filter { it.tagName != "script" }
		if (singleLine) {
			for (c in ls) {
				c.writeTo(buf, 0)
			}
		} else {
			for (c in ls) {
				buf.appendln()
				c.writeTo(buf, level + 1)
			}
		}
	}

	open fun writeChildren(singleLine: Boolean, buf: Appendable, level: Int) {
		writeChildren2(singleLine, buf, level)
		if (outputScript) {
			this.filterDeep { it.tagName == "script" && it.src.isNotEmpty() }.forEach {
				buf.appendln()
				it.writeTo(buf, level)
			}
			this.children.filter { it is ScriptBlock }.forEach {
				buf.appendln()
				it.writeTo(buf, level)
			}
			val ls = ArrayList<Tag>(16)
			this.children.forEach { c ->
				c.filterDeep(ls) { t ->
					t is ScriptBlock
				}
			}
			ls.forEach {
				buf.appendln()
				it.writeTo(buf, level)
			}
			buf.appendln()
		}
	}

	open fun writeTo(buf: Appendable, level: Int) {
		val singleLine = tagName in singleLineTags
		ident(buf, level)
		buf.append("<").append(tagName)
		attrs.put("class", clazz)
		for ((k, v) in attrs) {
			if (v.isEmpty()) {
				if (!isKeepAttr(k)) {
					continue
				}
			}
			buf.append(" ").append(k).append("=").append(attrVal(v))
		}
		if (children.isEmpty()) {
			if (tagName in selfEndTags) {
				buf.append("/>")
			} else {
				buf.append("></").append(tagName).append(">")
			}
			return
		} else {
			buf.append(">")
			writeChildren(singleLine, buf, level)
			if (!singleLine) {
				buf.appendln()
				ident(buf, level)
			}
			buf.append("</").append(tagName).append(">")
		}

	}

	override fun toHtml(): String {
		return this.toString()
	}

	override fun toString(): String {
		val n = preferBufferSize()
		val sb = StringBuilder(n)
		writeTo(sb, 0)
		return sb.toString()
	}

	open fun preferBufferSize(): Int {
		return tagName.length * 2 + attrs.size * 36 + 4 + 4 + children.sumBy { it.preferBufferSize() }
	}

	private fun isKeepAttr(key: String): Boolean {
		return "$tagName.$key" in keepAttrs
	}

	companion object {
		private var eleId: Int = 1

		//		val Null = HTag("null")
		//允许 <xxx  />
		val selfEndTags = setOf("meta", "link", "input", "img", "hr")
		val singleLineTags = setOf("span", "textarea", "label", "button", "title", "td", "th", "input", "option", "a", "h1", "h2", "h3", "h4", "h5", "h6")
		val keepAttrs = setOf("col.width", "option.value")
	}

}
