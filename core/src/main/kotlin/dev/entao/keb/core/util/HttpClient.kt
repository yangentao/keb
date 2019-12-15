@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PrivatePropertyName", "PropertyName", "FunctionName")

package dev.entao.keb.core.util

import dev.entao.kava.base.*
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import dev.entao.kava.log.logd
import dev.entao.kava.log.loge
import java.io.*
import java.net.*
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream


fun httpGet(url: String, block: HttpGet.() -> Unit): HttpResult {
	val h = HttpGet(url)
	h.block()
	return h.request()
}

fun httpPost(url: String, block: HttpPost.() -> Unit): HttpResult {
	val h = HttpPost(url)
	h.block()
	return h.request()
}

fun httpRaw(url: String, block: HttpRaw.() -> Unit): HttpResult {
	val h = HttpRaw(url)
	h.block()
	return h.request()
}

fun httpMultipart(url: String, block: HttpMultipart.() -> Unit): HttpResult {
	val h = HttpMultipart(url)
	h.block()
	return h.request()
}

/**
 * Created by entaoyang@163.com on 2016/12/20.
 */
class HttpGet(url: String) : HttpReq(url) {
	init {
		method = "GET"
	}

	override fun onSend(connection: HttpURLConnection) {
	}
}

class HttpPost(url: String) : HttpReq(url) {

	init {
		method = "POST"
		header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
	}

	override fun onSend(connection: HttpURLConnection) {
		val os = connection.outputStream
		try {
			val s = buildArgs()
			if (s.isNotEmpty()) {
				write(os, s)
				if (dumpReq) {
					logd("--body:", s)
				}
			}
			os.flush()
		} finally {
			os.closeSafe()
		}
	}
}

class HttpRaw(url: String) : HttpReq(url) {
	private lateinit var rawData: ByteArray

	init {
		method = "POST"
	}

	fun data(contentType: String, data: ByteArray): HttpRaw {
		header("Content-Type", contentType)
		this.rawData = data
		return this
	}

	fun jsonObject(block: YsonObject.() -> Unit): HttpRaw {
		val yo = YsonObject()
		yo.block()
		return this.json(yo.toString())
	}

	fun json(json: String): HttpRaw {
		return data("application/json;charset=utf-8", json.toByteArray(charsetUTF8))
	}

	fun xml(xml: String): HttpRaw {
		return data("application/xml;charset=utf-8", xml.toByteArray(charsetUTF8))
	}

	override fun onSend(connection: HttpURLConnection) {
		val os = connection.outputStream
		try {
			os.write(rawData)
			if (dumpReq && allowDump(this.headerMap["Content-Type"])) {
				logd("--body:", String(rawData, Charsets.UTF_8))
			}
			os.flush()
		} finally {
			os.closeSafe()
		}
	}
}

class HttpMultipart(url: String) : HttpReq(url) {
	private val BOUNDARY = UUID.randomUUID().toString()
	private val BOUNDARY_START = "--$BOUNDARY\r\n"
	private val BOUNDARY_END = "--$BOUNDARY--\r\n"

	private val fileList = ArrayList<FileParam>()

	init {
		method = "POST"
		header("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
	}

	fun file(fileParam: FileParam): HttpMultipart {
		fileList.add(fileParam)
		return this
	}

	fun file(key: String, file: File): HttpMultipart {
		val p = FileParam(key, file)
		return file(p)
	}


	fun file(key: String, file: File, block: FileParam.() -> Unit): HttpMultipart {
		val p = FileParam(key, file)
		p.block()
		return file(p)
	}

	override fun onSend(connection: HttpURLConnection) {
		val os = connection.outputStream
		try {
			sendMultipart(os)
			os.flush()
		} finally {
			os.closeSafe()
		}
	}

	override fun dumpReq() {
		super.dumpReq()
		for (fp in fileList) {
			logd("--file:", fp)
		}
	}

	override fun preConnect(connection: HttpURLConnection) {
		super.preConnect(connection)
		if (fileList.size > 0) {
			val os = SizeStream()
			sendMultipart(os)
			connection.setFixedLengthStreamingMode(os.size)
		}
	}

	@Throws(IOException::class)
	private fun sendMultipart(os: OutputStream) {
		if (argMap.size > 0) {
			for (e in argMap.entries) {
				write(os, BOUNDARY_START)
				write(os, "Content-Disposition: form-data; name=\"", e.key, "\"\r\n")
				write(os, "Content-Type:text/plain;charset=utf-8\r\n")
				write(os, "\r\n")
				write(os, e.value, "\r\n")
			}
		}
		if (fileList.size > 0) {
			for (fp in fileList) {
				write(os, BOUNDARY_START)
				write(os, "Content-Disposition:form-data;name=\"${fp.key}\";filename=\"${fp.filename}\"\r\n")
				write(os, "Content-Type:${fp.mime}\r\n")
				write(os, "Content-Transfer-Encoding: binary\r\n")
				write(os, "\r\n")
				val total = fp.file.length().toInt()
				if (os is SizeStream) {
					os.incSize(total)
				} else {
					copyStream(FileInputStream(fp.file), true, os, false, total, fp.progress)
				}
				write(os, "\r\n")
			}
		}
		os.write(BOUNDARY_END.toByteArray())
	}
}


abstract class HttpReq(val url: String) {

	val UTF8 = "UTF-8"
	val charsetUTF8 = Charsets.UTF_8

	protected var method: String = "GET"

	protected val headerMap = HashMap<String, String>()
	protected val argMap = HashMap<String, String>()


	private var timeoutConnect = 10000
	private var timeoutRead = 10000
	//	private var rawData: ByteArray? = null


	private var saveToFile: File? = null
	private var progress: Progress? = null

	var dumpReq: Boolean = false
	var dumpResp: Boolean = false

	init {
//		userAgent("android")
		accept("application/json,text/plain,text/html,*/*")
//		acceptLanguage("zh-CN,en-US;q=0.8,en;q=0.6")
		headerMap["Accept-Charset"] = "UTF-8,*"
		headerMap["Connection"] = "close"
//		headerMap["Charset"] = UTF8
	}

	fun saveTo(file: File): HttpReq {
		this.saveToFile = file
		return this
	}

	//recv progress
	fun progress(p: Progress?): HttpReq {
		this.progress = p
		return this
	}

	fun header(vararg pairs: Pair<String, String>): HttpReq {
		for ((k, v) in pairs) {
			headerMap[k] = v
		}
		return this
	}

	fun header(key: String, value: String): HttpReq {
		headerMap[key] = value
		return this
	}

	fun headers(map: Map<String, String>): HttpReq {
		headerMap.putAll(map)
		return this
	}

	fun timeoutConnect(millSeconds: Int): HttpReq {
		this.timeoutConnect = millSeconds
		return this
	}

	fun timeoutRead(millSeconds: Int): HttpReq {
		this.timeoutRead = millSeconds
		return this
	}

	fun accept(accept: String): HttpReq {
		headerMap["Accept"] = accept
		return this
	}

	fun acceptLanguage(acceptLanguage: String): HttpReq {
		headerMap["Accept-Language"] = acceptLanguage
		return this
	}

	fun auth(user: String, pwd: String): HttpReq {
		val usernamePassword = "$user:$pwd"
		val encodedUsernamePassword = Base64.getUrlEncoder().encodeToString(usernamePassword.toByteArray(charsetUTF8))
		headerMap["Authorization"] = "Basic $encodedUsernamePassword"
		return this
	}

	fun autoBearer(token: String): HttpReq {
		headerMap["Authorization"] = "Bearer $token"
		return this
	}

	fun userAgent(userAgent: String): HttpReq {
		return header("User-Agent", userAgent)
	}

	infix fun String.to(v: String) {
		arg(this, v)
	}

	infix fun String.to(v: Int) {
		arg(this, v.toString())
	}

	infix fun String.to(v: Long) {
		arg(this, v.toString())
	}

	infix fun String.to(v: Double) {
		arg(this, v.toString())
	}

	infix fun String.to(v: Boolean) {
		arg(this, v.toString())
	}

	fun arg(key: String, value: String): HttpReq {
		argMap[key] = value
		return this
	}

	fun arg(key: String, value: Long): HttpReq {
		argMap[key] = "" + value
		return this
	}


	fun args(vararg args: Pair<String, String>): HttpReq {
		for ((k, v) in args) {
			argMap[k] = v
		}
		return this
	}

	fun args(map: Map<String, String>): HttpReq {
		argMap.putAll(map)
		return this
	}


	//[from, to]
	fun range(from: Int, to: Int): HttpReq {
		headerMap["Range"] = "bytes=$from-$to"
		return this
	}

	fun range(from: Int): HttpReq {
		headerMap["Range"] = "bytes=$from-"
		return this
	}

	protected fun buildArgs(): String {
		return argMap.map {
			it.key.urlEncoded + "=" + it.value.urlEncoded
		}.joinToString("&")
	}

	@Throws(MalformedURLException::class)
	fun buildGetUrl(): String {
		val sArgs = buildArgs()
		var u: String = url
		if (sArgs.isNotEmpty()) {
			val n = u.indexOf('?')
			if (n < 0) {
				u += "?"
			}
			if ('?' != u[u.length - 1]) {
				u += "&"
			}
			u += sArgs
		}
		return u
	}

	open fun dumpReq() {
		if (!dumpReq) {
			return
		}
		logd("Http Request:", url)
		for ((k, v) in headerMap) {
			logd("--head:", k, "=", v)
		}
		for ((k, v) in argMap) {
			logd("--arg:", k, "=", v)
		}
	}

	@Throws(ProtocolException::class, UnsupportedEncodingException::class)
	protected open fun preConnect(connection: HttpURLConnection) {
		HttpURLConnection.setFollowRedirects(true)
		connection.doOutput = method != "GET"
		connection.doInput = true
		connection.connectTimeout = timeoutConnect
		connection.readTimeout = timeoutRead
		connection.requestMethod = method
		connection.useCaches = false

		for (e in headerMap.entries) {
			connection.setRequestProperty(e.key, e.value)
		}
	}

	@Throws(IOException::class)
	private fun onResponse(connection: HttpURLConnection): HttpResult {
		val result = HttpResult(this.url)
		result.responseCode = connection.responseCode
		result.responseMsg = connection.responseMessage
		result.contentType = connection.contentType
		result.headerMap = connection.headerFields
		val total = connection.contentLength
		result.contentLength = total
		val os: OutputStream = if (this.saveToFile != null) {
			val dir = this.saveToFile!!.parentFile
			if (dir != null) {
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						loge("创建目录失败")
						throw IOException("创建目录失败!")
					}
				}
			}
			FileOutputStream(saveToFile!!)
		} else {
			ByteArrayOutputStream(if (total > 0) total else 64)
		}
		var input = connection.inputStream
		val mayGzip = connection.contentEncoding
		if (mayGzip != null && mayGzip.contains("gzip")) {
			input = GZIPInputStream(input)
		}
		copyStream(input, true, os, true, total, progress)
		if (os is ByteArrayOutputStream) {
			result.response = os.toByteArray()
		}
		return result
	}

	@Throws(IOException::class)
	protected abstract fun onSend(connection: HttpURLConnection)


	fun request(): HttpResult {
		var connection: HttpURLConnection? = null
		try {
			dumpReq()
			connection = if (this is HttpGet || this is HttpRaw) {
				URL(buildGetUrl()).openConnection() as HttpURLConnection
			} else {
				URL(url).openConnection() as HttpURLConnection
			}

			preConnect(connection)
			connection.connect()
			onSend(connection)
			val r = onResponse(connection)
			if (dumpResp) {
				r.dump()
			}
			return r
		} catch (ex: Exception) {
			ex.printStackTrace()
			loge(ex)
			val result = HttpResult(this.url)
			result.exception = ex
			return result
		} finally {
			connection?.disconnect()
		}
	}

	fun download(saveto: File, progress: Progress?): HttpResult {
		return saveTo(saveto).progress(progress).request()
	}
}

@Throws(IOException::class)
private fun write(os: OutputStream, vararg arr: String) {
	for (s in arr) {
		os.write(s.toByteArray(Charsets.UTF_8))
	}
}

private fun allowDump(ct: String?): Boolean {
	val a = ct?.toLowerCase() ?: return false
	return "json" in a || "xml" in a || "html" in a || "text" in a
}

//file, key, filename, mime都不能是空
class FileParam(val key: String, val file: File, var filename: String = file.name, var mime: String = "application/octet-stream") {
	var progress: Progress? = null

	fun mime(mime: String?): FileParam {
		if (mime != null) {
			this.mime = mime
		}
		return this
	}

	fun fileName(filename: String?): FileParam {
		if (filename != null) {
			this.filename = filename
		}
		return this
	}

	fun progress(progress: Progress?): FileParam {
		this.progress = progress
		return this
	}

	override fun toString(): String {
		return "key=$key, filename=$filename, mime=$mime, file=$file"
	}
}


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

	val contentCharset: Charset?
		get() {
			val ct = this.contentType ?: return null
			val ls: List<String> = ct.split(";".toRegex()).dropLastWhile { it.isEmpty() }
			for (item in ls) {
				val ss = item.trim()
				if (ss.startsWith("charset")) {
					val charset = ss.substringAfterLast('=', "")
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

//fun main() {
//	val url = "http://localhost:8080/taoke/userapi/login"
//	val h = HttpGet(url)
//	h.dumpReq = true
//	h.dumpResp = true
//	h.arg("user", "yang")
//	h.arg("pwd", "entao")
//	val r = h.request()
//	logd(r.strUtf8())
//	logd("END")
//}
