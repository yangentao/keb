@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.core

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.firstParamName
import dev.entao.keb.core.render.FileSender
import dev.entao.keb.core.render.ResultRender
import dev.entao.keb.core.util.AnyMap
import java.io.File
import java.net.URLEncoder
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.servlet.http.Part
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Created by entaoyang@163.com on 2016/12/18.
 */

class HttpContext(val filter: HttpFilter, val request: HttpServletRequest, val response: HttpServletResponse, val chain: FilterChain) {

	val fileSender: FileSender by lazy {
		FileSender(this)
	}
	val resultSender: ResultRender by lazy {
		ResultRender(this)
	}
	val httpParams: HttpParams  by lazy {
		HttpParams(this)
	}

	val currentUri: String = request.requestURI.trimEnd('/').toLowerCase()

	val propMap = AnyMap()

	//解析后的app传来的access_token
	var accessToken: String by propMap
	var tokenBody: String by propMap
	var tokenHeader: String by propMap
	var tokenOK: Boolean by propMap

	//web登录成功后, 设置account和accountName
	fun setLoginAccount(account: String) {
		putSession(Keb.ACCOUNT, account)
	}

	//登出后调用, 清除session
	fun clearLoginAccount() {
		removeSession(Keb.ACCOUNT)
	}

	//web登录成功后, 设置account和accountName
	val account: String
		get() {
			return getSession(Keb.ACCOUNT) ?: ""
		}
	var accountName: String
		get() {
			val a = this.account
			if (a.isEmpty()) {
				return ""
			}
			return getSession(Keb.ACCOUNT_NAME) ?: a
		}
		set(value) {
			putSession(Keb.ACCOUNT_NAME, value)
		}
	val isLogined: Boolean
		get() {
			return account.isNotEmpty()
		}

	val rootUri: String
		get() {
			return buildPath(filter.contextPath)
		}

	fun fullUrlOf(uri: String): String {
		return request.scheme + "://" + request.getHeader("host") + uri
	}

	fun groupUri(g: KClass<*>): String {
		return filter.groupUri(g)
	}

	fun actionUri(a: KFunction<*>): String {
		return filter.actionUri(a)
	}

	fun actionUri(a: KFunction<*>, paramValue: Any?): String {
		val s = filter.actionUri(a)
		if (paramValue != null) {
			val k = a.firstParamName
			if (k != null) {
				return s + "?" + k + "=" + URLEncoder.encode(paramValue.toString(), Charsets.UTF_8.name())
			}
		}
		return s
	}

	fun resUri(res: String): String {
		return filter.resUri(res)
	}

	fun writeHtml(s: String) {
		this.response.contentTypeHtml()
		this.response.writer.write(s)
	}

	fun writeTextPlain(s: String) {
		this.response.contentType = Mimes.PLAIN
		this.response.writer.write(s)
	}

	fun writeXML(s: String) {
		this.response.contentType = Mimes.XML
		this.response.writer.write(s)
	}

	fun writeJSON(s: String) {
		this.response.contentType = Mimes.JSON
		this.response.writer.write(s)
	}

	val acceptJson: Boolean
		get() {
			return request.headerJson
		}
	val acceptHtml: Boolean
		get() {
			return Mimes.HTML in request.header("Accept") ?: ""
		}

	@Suppress("UNUSED_PARAMETER")
	fun allow(uri: String): Boolean {
		return true
	}

	fun redirect(url: String) {
		response.sendRedirect(url)
	}

	fun backward(block: ReferUrl.() -> Unit): Boolean {
		val s = request.headerReferer
		if (s == null || s.isEmpty()) {
			return false
		}
		val u = ReferUrl(request)
		u.block()
		val url = u.build()
		response.sendRedirect(url)
		return true
	}

	fun respHeader(key: String, value: String) {
		response.addHeader(key, value)
	}

	fun reqHeader(key: String): String? {
		return request.getHeader(key)
	}

	fun getSession(key: String): String? {
		val se: HttpSession = request.getSession(false) ?: return null
		return se.getAttribute(key) as? String
	}

	fun putSession(key: String, value: String) {
		val se: HttpSession = request.getSession(true)
		se.setAttribute(key, value)
		se.maxInactiveInterval = filter.sessionTimeoutSeconds

	}

	fun removeSession(key: String) {
		val se: HttpSession = request.getSession(false) ?: return
		se.removeAttribute(key)
	}

	fun part(name: String): Part? {
		return request.part(name)
	}

	fun parts(): List<Part> {
		return request.parts.toList()
	}

	fun fileParts(): List<Part> {
		return parts().filter { it.submittedFileName != null && it.submittedFileName.isNotEmpty() }
	}

	val firstFilePart: Part?
		get() {
			return this.fileParts().firstOrNull()
		}

	fun abort(code: Int) {
		response.sendError(code)
	}

	fun abort(code: Int, msg: String) {
		response.sendError(code, msg)
	}

	val uploadDir: File get() = filter.webDir.uploadDir
	val tmpDir: File get() = filter.webDir.tmpDir

}
