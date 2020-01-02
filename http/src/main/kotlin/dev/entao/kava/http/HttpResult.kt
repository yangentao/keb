@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.kava.http

import dev.entao.kava.base.closeSafe
import dev.entao.json.YsonArray
import dev.entao.json.YsonObject
import dev.entao.kava.log.logd
import dev.entao.kava.log.loge
import java.io.File
import java.io.FileOutputStream
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.concurrent.TimeoutException


class HttpResult(val url: String) {
	var response: ByteArray? = null//如果Http.request参数给定了文件参数, 则,response是null
	var responseCode: Int = 0//200
	var responseMsg: String? = null//OK
	var contentType: String? = null
	var contentLength: Int = 0//如果是gzip格式, 这个值!=response.length
	var headerMap: Map<String, List<String>>? = null
	var exception: Exception? = null

	var needDecode: Boolean = false

	val OK: Boolean get() = responseCode in 200..299

	val errorMsg: String?
		get() {
			val ex = exception
			return when (ex) {
				null -> httpMsgByCode(responseCode)
				is NoRouteToHostException -> "网络不可达"
				is TimeoutException -> "请求超时"
				is SocketTimeoutException -> "请求超时"
				is SocketException -> "网络错误"
				else -> ex.message
			}
		}
	//Content-Type: text/html; charset=GBK
	val contentCharset: Charset?
		get() {
			val ct = contentType ?: return null
			val ls: List<String> = ct.split(";".toRegex()).dropLastWhile { it.isEmpty() }
			for (item in ls) {
				val ss = item.trim();
				if (ss.startsWith("charset")) {
					val charset = ss.substringAfterLast('=', "").trim()
					if (charset.length >= 2) {
						return Charset.forName(charset)
					}
				}
			}
			return null
		}

	fun responseText(charset: Charset = Charsets.UTF_8): String? {
		val r = this.response ?: return null
		val ch = contentCharset ?: charset
		var s = String(r, ch)
		if (needDecode) {
			s = URLDecoder.decode(s, ch.name())
		}
		return s
	}

	fun dump() {
		logd(">>Response:", this.url)
		logd("  >>status:", responseCode, responseMsg ?: "")
		val map = this.headerMap
		if (map != null) {
			for ((k, v) in map) {
				if (v.size == 1) {
					logd("  >>head:", k, "=", v.first())
				} else {
					logd("  >>head:", k, "=", "[" + v.joinToString(",") + "]")
				}
			}
		}
		if (allowDump(this.contentType)) {
			logd("  >>body:", this.responseText())
		}
	}

	fun needDecode(): HttpResult {
		this.needDecode = true
		return this
	}

	fun str(charset: Charset): String? {
		if (OK) {
			return this.responseText(charset)
		}
		return null
	}

	fun strISO8859_1(): String? = str(Charsets.ISO_8859_1)

	fun strUtf8(): String? = str(Charsets.UTF_8)

	fun <T> textTo(block: (String) -> T): T? {
		if (OK) {
			val s = strUtf8()
			if (s != null && s.isNotEmpty()) {
				try {
					return block(s)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
		return null
	}

	fun ysonArray(): YsonArray? {
		return textTo { YsonArray(it) }
	}

	fun ysonObject(): YsonObject? {
		return textTo { YsonObject(it) }
	}

	fun bytes(): ByteArray? {
		if (OK) {
			return response
		}
		return null
	}

	fun saveTo(file: File): Boolean {
		val data = this.response ?: return false
		if (OK) {
			val dir = file.parentFile
			if (dir != null) {
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						loge("创建目录失败")
						return false
					}
				}
			}
			var fos: FileOutputStream? = null
			try {
				fos = FileOutputStream(file)
				fos.write(data)
				fos.flush()
			} catch (ex: Exception) {
				ex.printStackTrace()
			} finally {
				fos?.closeSafe()
			}
		}
		return false
	}

}