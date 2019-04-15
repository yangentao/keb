@file:Suppress("unused")

package dev.entao.keb.core

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.matchIp4
import dev.entao.kava.log.logd
import java.nio.charset.Charset
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Part

/**
 * Created by entaoyang@163.com on 2017/4/4.
 */

val ServletRequest.method: String get() = (this as HttpServletRequest).method

val ServletRequest.isGet: Boolean get() = "GET" == method

val ServletRequest.isPost: Boolean get() = "POST" == method

val ServletRequest.isHead: Boolean get() = "HEAD" == method

val ServletRequest.isPut: Boolean get() = "PUT" == method

val ServletRequest.isDelete: Boolean get() = "DELETE" == method

val ServletRequest.isOptions: Boolean get() = "OPTIONS" == method

val ServletRequest.isTrace: Boolean get() = "TRACE" == method

fun HttpServletRequest.dumpParam() {
	this.paramMap.forEach { e ->
		logd(e.key, " = ", e.value)
	}
}

fun HttpServletRequest.attr(name: String): Any? {
	return this.getAttribute(name)
}

fun HttpServletRequest.attr(name: String, value: Any?) {
	this.setAttribute(name, value)
}

val HttpServletRequest.attrNameSet: Set<String> get() = enumToSet(this.attributeNames)

fun HttpServletRequest.param(name: String): String? {
	return this.getParameter(name)
}

fun HttpServletRequest.paramValues(name: String): Array<String> {
	return this.getParameterValues(name)
}

val HttpServletRequest.paramNameSet: Set<String> get() = enumToSet(this.parameterNames)

val HttpServletRequest.paramMap: Map<String, Array<String>> get() = this.parameterMap

val HttpServletRequest.localeSet: Set<Locale> get() = enumToSet(this.locales)

fun HttpServletRequest.header(name: String): String? {
	return this.getHeader(name)
}

val HttpServletRequest.headerNameSet: Set<String> get() = enumToSet(this.headerNames)
fun HttpServletRequest.headerValues(name: String): Set<String> {
	return enumToSet(this.getHeaders(name))
}

val HttpServletRequest.headerReferer: String?
	get() {
		val s = this.getHeader("Referer")
		if (s != null && s.isNotEmpty()) {
			return s
		}
		return null
	}
val HttpServletRequest.headerUserAgent: String?
	get() {
		return this.getHeader("User-Agent")
	}
val HttpServletRequest.headerAccept: String?
	get() {
		return this.getHeader("Accept")
	}
val HttpServletRequest.headerAcceptLanguage: String?
	get() {
		return this.getHeader("Accept-Language")
	}
val HttpServletRequest.headerJson: Boolean
	get() {
		return headerAccept?.contains(Mimes.JSON) ?: false
	}

val HttpServletRequest.filePart: Part?
	get() {
		return this.fileParts.firstOrNull()
	}

val HttpServletRequest.fileParts: List<Part>
	get() {
		return parts.filter { it.submittedFileName != null && it.submittedFileName.isNotEmpty() }
	}

fun HttpServletRequest.part(name: String): Part? {
	try {
		return this.getPart(name)
	} catch (e: Exception) {
		e.printStackTrace()
	}
	return null
}

fun HttpServletRequest.partParam(name: String, defaultCharset: Charset = Charsets.UTF_8): String? {
	val p = this.part(name) ?: return null
	val buf = p.inputStream.readBytes()
	val ct = findCharsetFromContentType(p.contentType) ?: defaultCharset
	val value = String(buf, ct)
	return value
}

//text/plain;charset=utf-8  => UTF-8
private fun findCharsetFromContentType(contentType: String): Charset? {
	for (s in contentType.split(';')) {
		if ("charset" in s) {
			val arr = s.split('=')
			if (arr.size == 2) {
				val cs = arr[1].trim().toUpperCase()
				if (cs.isNotEmpty()) {
					return Charset.forName(cs)
				}
			}
		}
	}
	return null
}

private fun <T> enumToSet(e: Enumeration<T>): HashSet<T> {
	val set = HashSet<T>(32)
	while (e.hasMoreElements()) {
		set.add(e.nextElement())
	}
	return set
}

//response exten
fun HttpServletResponse.print(s: String) {
	this.writer.print(s)
}

fun HttpServletResponse.contentTypeJson() {
	this.contentType = Mimes.JSON
}

fun HttpServletResponse.contentTypeHtml() {
	this.contentType = "${Mimes.HTML};charset=UTF-8"
}

fun HttpServletResponse.contentTypePlain() {
	this.contentType = Mimes.PLAIN
}

fun HttpServletResponse.contentTypeXml() {
	this.contentType = Mimes.XML
}

fun HttpServletResponse.contentTypeStream() {
	this.contentType = Mimes.STREAM
}

val HttpServletRequest.clientIp: String
	get() {
		val a = header("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
		if (a != null && a.matchIp4()) {
			return a
		}
		val b = header("X-Real-IP")?.trim()
		if (b != null && b.matchIp4()) {
			return b
		}
		return this.remoteAddr
	}


