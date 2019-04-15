package dev.entao.ken.client

import dev.entao.kava.log.loge
import dev.entao.kava.base.closeSafe
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 * Created by entaoyang@163.com on 2016/12/20.
 */

class HttpResult {
	var response: ByteArray? = null//如果Http.request参数给定了文件参数, 则,response是null
	var responseCode: Int = 0//200
	var responseMsg: String? = null//OK
	var contentType: String? = null//text/html;charset=utf-8
		set(value) {
			field = value
			if (value != null && value.startsWith("text/html")) {
				needDecode = true
			}
		}
	var contentLength: Int = 0//如果是gzip格式, 这个值!=response.length
	var headerMap: Map<String, List<String>>? = null
	var exception: Exception? = null

	var needDecode: Boolean = false

	var OK: Boolean = false
		get() = responseCode >= 200 && responseCode < 300

	fun OK(): Boolean {
		return OK
	}

	val contentCharset: Charset?
		get() {
			if (contentType != null) {
				var ls: List<String> = contentType!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }
				for (item in ls) {
					var ss = item.trim();
					if (ss.startsWith("charset")) {
						val charset = ss.substringAfterLast('=', "");
						if (charset.length >= 2) {
							return Charset.forName(charset)
						}
					}
				}
			}
			return null
		}

	fun needDecode(): HttpResult {
		this.needDecode = true
		return this
	}

	fun str(defCharset: Charset): String? {
		if (OK()) {
			if (response != null) {
				var s = String(response!!, contentCharset ?: defCharset)
				if (needDecode) {
					s = URLDecoder.decode(s, defCharset.name())
				}
				return s
			}
		}
		return null
	}

	fun strISO8859_1(): String? = str(Charsets.ISO_8859_1)
	fun strUtf8(): String? = str(Charsets.UTF_8)

	fun <T> textTo(block: (String) -> T): T? {
		if (OK()) {
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
		if (OK()) {
			return response
		}
		return null
	}

	fun saveTo(file: File): Boolean {
		if (OK()) {
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
				fos.write(response)
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