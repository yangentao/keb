@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.core

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.firstParamName
import dev.entao.keb.core.render.FileSender
import java.io.File
import java.net.URLEncoder
import java.util.HashMap
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
	val httpParams: HttpParams  by lazy {
		HttpParams(this)
	}

	val currentUri: String = request.requestURI.trimEnd('/').toLowerCase()

	val ctxMap = HashMap<String, Any>()


	var os: String = ""
	var token: String = ""
	var userId: Int = 0
	var accountId: Int = 0
	var userName: String = ""
	var accountName: String = ""

	val loginedApp: Boolean get() = userId != 0
	val loginedWeb: Boolean get() = accountId != 0



	val rootUri: String
		get() {
			return buildPath(filter.contextPath)
		}

	fun fullUrlOf(uri: String): String {
		return "http://" + request.getHeader("host") + uri
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

	fun loginWeb(accountId: Int) {
		putSession(ACCOUNT_ID, accountId.toString())
	}

	fun logoutWeb() {
		removeSession(ACCOUNT_ID)
		accountId = 0
	}

	val acceptJson: Boolean
		get() {
			return request.headerJson
		}
	val acceptHtml: Boolean
		get() {
			return Mimes.HTML in request.header("Accept") ?: ""
		}

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

	fun abort(code: Int) {
		response.sendError(code)
	}

	fun abort(code: Int, msg: String) {
		response.sendError(code, msg)
	}

	val uploadDir: File get() = filter.webDir.uploadDir
	val tmpDir: File get() = filter.webDir.tmpDir

	companion object {
		const val ACCOUNT_ID = "accountId"
		const val TOKEN = "token"
	}
}
