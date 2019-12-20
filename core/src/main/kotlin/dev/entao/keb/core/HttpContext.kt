@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.core

import dev.entao.kava.base.Mimes
import dev.entao.keb.core.render.FileSender
import dev.entao.keb.core.render.ResultRender
import dev.entao.keb.core.util.AnyMap
import java.io.File
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.servlet.http.Part

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
	val httpParams: HttpParams by lazy {
		HttpParams(this)
	}
	val params: HttpParams get() = this.httpParams
	val currentUri: String by lazy { request.requestURI.trimEnd('/').toLowerCase() }

	val propMap = AnyMap()

	//TODO dirs
	val uploadDir: File get() = filter.webDir.uploadDir
	val tmpDir: File get() = filter.webDir.tmpDir

	val rootUri: String
		get() {
			return buildPath(filter.contextPath)
		}

	fun fullUrlOf(uri: String): String {
		return request.scheme + "://" + request.getHeader("host") + uri
	}

	fun fullURL(action: HttpAction): String {
		return this.fullUrlOf(actionUri(action))
	}

	fun resUri(res: String): String {
		return filter.resUri(res)
	}

	fun actionUri(action: HttpAction): String {
		return this.filter.actionUri(action)
	}


	fun writeHtml(s: String) {
		this.response.contentTypeHtml()
		val w = this.response.writer
		w.write(s)
		w.flush()
	}

	fun writeTextPlain(s: String) {
		this.response.contentType = Mimes.PLAIN
		val w = this.response.writer
		w.write(s)
		w.flush()
	}

	fun writeXML(s: String) {
		this.response.contentType = Mimes.XML
		val w = this.response.writer
		w.write(s)
		w.flush()
	}

	fun writeJSON(s: String) {
		this.response.contentType = Mimes.JSON
		val w = this.response.writer
		w.write(s)
		w.flush()
	}

	val acceptJson: Boolean
		get() {
			return request.headerJson
		}
	val acceptHtml: Boolean
		get() {
			return Mimes.HTML in request.header("Accept") ?: ""
		}


	fun redirect(url: String) {
		response.sendRedirect(url)
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

	fun backSuccess(msg: String): Boolean {
		val s = request.headerReferer
		if (s == null || s.isEmpty()) {
			return false
		}
		val url = Url(s)
		url.remove(Keb.ERROR)
		url.replace(Keb.SUCCESS, msg)
		response.sendRedirect(url.build())
		return true
	}

	fun backError(msg: String, errorField: String = ""): Boolean {
		val s = request.headerReferer
		if (s == null || s.isEmpty()) {
			return false
		}
		val url = Url(s)
		val map = request.paramMap
		map.forEach {
			val k = it.key
			val ar = it.value
			url.remove(k)
			if (ar.size == 1) {
				url.append(k, ar[0])
			} else if (ar.size > 1) {
				for (v in ar) {
					url.append("$k[]", v)
				}
			}
		}
		if (errorField.isNotEmpty()) {
			url.replace(Keb.errField(errorField), msg)
		}
		url.remove(Keb.SUCCESS)
		url.replace(Keb.ERROR, msg)
		response.sendRedirect(url.build())
		return true
	}


}
