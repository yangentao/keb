@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.ken

import dev.entao.kava.base.Mimes
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.servlet.http.Part
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance

/**
 * Created by entaoyang@163.com on 2016/12/18.
 */

class HttpContext(val filter: HttpFilter, val request: HttpServletRequest, val response: HttpServletResponse) {

	val htmlSender: HtmlSender  by lazy {
		HtmlSender(this)
	}
	val jsonSender: JsonSender  by lazy {
		JsonSender(this)
	}
	val xmlSender: XmlSender by lazy {
		XmlSender(this)
	}
	val resultSender: ResultSender  by lazy {
		ResultSender(this)
	}
	val fileSender: FileSender by lazy {
		FileSender(this)
	}
	val httpParams: HttpParams  by lazy {
		HttpParams(this)
	}

	val path: WebPath get() = WebPath(this.filter)

	var os: String = ""
	var token: String = ""
	var userId: Int = 0
	var accountId: Int = 0
	var userName: String = ""
	var accountName: String = ""

	val loginedApp: Boolean get() = userId != 0
	val loginedWeb: Boolean get() = accountId != 0

	private val permAcceptor: PermAcceptor? = filter.permAcceptorClass?.createInstance()

	init {
		permAcceptor?.prepare(this)
	}

	fun allow(action: HttpAction): Boolean {
		return allow(path.action(action).uri)
	}

	fun allow(uri: String): Boolean {
		return permAcceptor?.accept(this, uri) ?: true
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

	//war解压后的目录,  WEB-INF和META-INF所在的目录
	val localAppDir: File
		get() {
			val s = filter.filterConfig.servletContext.getRealPath("/")
			return File(s)
		}

	fun redirect(url: String) {
		response.sendRedirect(url)
	}

	fun redirect(p: WebPath, block: WebPath.() -> Unit = {}) {
		p.block()
		response.sendRedirect(p.uri)
	}

	fun redirect(f: KFunction<*>, block: WebPath.() -> Unit = {}) {
		val p = WebPath(filter, f)
		p.block()
		redirect(p)
	}

	fun forward(p: WebPath) {
		request.getRequestDispatcher(p.uriApp).forward(request, response)
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

	val uploadDir: File get() = filter.uploadDir
	val tmpDir: File get() = filter.tmpDir

	companion object {
		const val ACCOUNT_ID = "accountId"
		const val TOKEN = "token"
	}
}
