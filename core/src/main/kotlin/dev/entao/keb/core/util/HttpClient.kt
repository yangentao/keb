@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PrivatePropertyName", "PropertyName", "FunctionName")

package dev.entao.keb.core.util

import dev.entao.kava.base.*
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import dev.entao.kava.log.log
import dev.entao.kava.log.logd
import dev.entao.kava.log.loge
import java.io.*
import java.net.*
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream

/**
 * Created by entaoyang@163.com on 2016/12/20.
 */


class Http(val url: String) {
	enum class HttpMethod {
		GET, POST, POST_MULTIPART, POST_RAW_DATA
	}

	val UTF8 = "UTF-8"
	val charsetUTF8 = Charsets.UTF_8

	private val BOUNDARY = UUID.randomUUID().toString()
	private val BOUNDARY_START = "--$BOUNDARY\r\n"
	private val BOUNDARY_END = "--$BOUNDARY--\r\n"


	private var method: HttpMethod = HttpMethod.GET

	private val headerMap = HashMap<String, String>()
	private val argMap = HashMap<String, String>()

	private val fileList = ArrayList<FileParam>()

	private var timeoutConnect = 10000
	private var timeoutRead = 10000
	//	private var rawData: ByteArray? = null
	private var rawData: ByteArray? = null

	private var saveToFile: File? = null
	private var progress: Progress? = null

	var dumpReq: Boolean = false
	var dumpResp: Boolean = false

	init {
//		userAgent("android")
		accept("application/json,text/plain,text/html,*/*")
//		acceptLanguage("zh-CN,en-US;q=0.8,en;q=0.6")
//		headerMap["Accept-Charset"] = "UTF-8,*"
		headerMap["Connection"] = "close"
//		headerMap["Charset"] = UTF8
	}

	fun saveTo(file: File): Http {
		this.saveToFile = file
		return this
	}

	//recv progress
	fun progress(p: Progress?): Http {
		this.progress = p
		return this
	}

	fun header(vararg pairs: Pair<String, String>): Http {
		for ((k, v) in pairs) {
			headerMap[k] = v
		}
		return this
	}

	fun header(key: String, value: String): Http {
		headerMap[key] = value
		return this
	}

	fun headers(map: Map<String, String>): Http {
		headerMap.putAll(map)
		return this
	}

	fun timeoutConnect(millSeconds: Int): Http {
		this.timeoutConnect = millSeconds
		return this
	}

	fun timeoutRead(millSeconds: Int): Http {
		this.timeoutRead = millSeconds
		return this
	}

	/**
	 * @param accept "* / *", " plain/text"
	 * *
	 * @return
	 */
	fun accept(accept: String): Http {
		headerMap["Accept"] = accept
		return this
	}

	fun acceptLanguage(acceptLanguage: String): Http {
		headerMap["Accept-Language"] = acceptLanguage
		return this
	}

	fun auth(user: String, pwd: String): Http {
		val usernamePassword = "$user:$pwd"
		val encodedUsernamePassword = Base64.getUrlEncoder().encodeToString(usernamePassword.toByteArray(charsetUTF8))
		headerMap["Authorization"] = "Basic $encodedUsernamePassword"
		return this
	}

	fun autoBearer(token: String): Http {
		headerMap["Authorization"] = "Bearer $token"
		return this
	}

	fun userAgent(userAgent: String): Http {
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

	fun arg(key: String, value: String): Http {
		argMap[key] = value
		return this
	}

	fun arg(key: String, value: Long): Http {
		argMap[key] = "" + value
		return this
	}


	fun args(vararg args: Pair<String, String>): Http {
		for ((k, v) in args) {
			argMap[k] = v
		}
		return this
	}

	fun args(map: Map<String, String>): Http {
		argMap.putAll(map)
		return this
	}


	fun file(fileParam: FileParam): Http {
		fileList.add(fileParam)
		return this
	}

	fun file(key: String, file: File): Http {
		val p = FileParam(key, file)
		return file(p)
	}


	fun file(key: String, file: File, block: FileParam.() -> Unit): Http {
		val p = FileParam(key, file)
		p.block()
		return file(p)
	}


	/**
	 * [from, to]

	 * @param from
	 * *
	 * @param to
	 * *
	 * @return
	 */
	fun range(from: Int, to: Int): Http {
		headerMap["Range"] = "bytes=$from-$to"
		return this
	}

	fun range(from: Int): Http {
		headerMap["Range"] = "bytes=$from-"
		return this
	}

	@Throws(ProtocolException::class, UnsupportedEncodingException::class)
	private fun preConnect(connection: HttpURLConnection) {
		HttpURLConnection.setFollowRedirects(true)
		connection.doOutput = method != HttpMethod.GET
		connection.doInput = true
		connection.connectTimeout = timeoutConnect
		connection.readTimeout = timeoutRead
		if (method == HttpMethod.GET) {
			connection.requestMethod = "GET"
		} else {
			connection.requestMethod = "POST"
			connection.useCaches = false
		}

		for (e in headerMap.entries) {
			connection.setRequestProperty(e.key, e.value)
		}
		if (fileList.size > 0) {
			val os = SizeStream()
			sendMultipart(os)
			connection.setFixedLengthStreamingMode(os.size)
		}
	}

	@Throws(IOException::class)
	private fun write(os: OutputStream, vararg arr: String) {
		for (s in arr) {
			os.write(s.toByteArray(charsetUTF8))
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
				val progress = fp.progress
				val fis = FileInputStream(fp.file)
				val total = fp.file.length().toInt()
				if (os is SizeStream) {
					os.incSize(total)
					fis.closeSafe()
				} else {
					copyStream(fis, true, os, false, total, progress)
				}
				write(os, "\r\n")
			}
		}
		os.write(BOUNDARY_END.toByteArray())
	}

	private fun buildArgs(): String {
		val sb = StringBuilder(argMap.size * 32 + 16)
		for (e in argMap.entries) {
			try {
				val name = e.key.urlEncoded
				val value = e.value.urlEncoded
				if (sb.isNotEmpty()) {
					sb.append("&")
				}
				sb.append(name)
				sb.append("=")
				sb.append(value)
			} catch (ex: Exception) {
				ex.printStackTrace()
			}

		}
		return sb.toString()
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

	@Throws(IOException::class)
	private fun onResponse(connection: HttpURLConnection): HttpResult {
		val result = HttpResult(this.url)
		result.responseCode = connection.responseCode
		result.responseMsg = connection.responseMessage
		result.contentType = connection.contentType
		result.headerMap = connection.headerFields
		val total = connection.contentLength
		result.contentLength = total

		if (dumpResp) {
			result.dump()
		}
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
			FileOutputStream(saveToFile)
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
	private fun onSend(connection: HttpURLConnection) {
		if (HttpMethod.GET == method) {
			return
		}
		val os = connection.outputStream
		try {
			when (method) {
				HttpMethod.POST -> {
					val s = buildArgs()
					if (s.isNotEmpty()) {
						write(os, s)
					}
				}
				HttpMethod.POST_MULTIPART -> sendMultipart(os)
				HttpMethod.POST_RAW_DATA -> os.write(rawData!!)
				else -> {
				}
			}
			os.flush()
		} finally {
			os.closeSafe()
		}
	}

	fun dumpReq() {
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
		for (fp in fileList) {
			logd("--file:", fp)
		}
	}

	private fun request(): HttpResult {
		var connection: HttpURLConnection? = null
		try {
			dumpReq()
			connection = if (method == HttpMethod.GET || method == HttpMethod.POST_RAW_DATA) {
				URL(buildGetUrl()).openConnection() as HttpURLConnection
			} else {
				URL(url).openConnection() as HttpURLConnection
			}

			preConnect(connection)
			connection.connect()
			onSend(connection)
			return onResponse(connection)
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

	fun get(): HttpResult {
		method = HttpMethod.GET
		return request()
	}

	fun post(): HttpResult {
		method = HttpMethod.POST
		header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
		return request()
	}

	fun multipart(): HttpResult {
		method = HttpMethod.POST_MULTIPART
		header("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
		return request()
	}

	fun postRawData(contentType: String, data: ByteArray): HttpResult {
		method = HttpMethod.POST_RAW_DATA
		header("Content-Type", contentType)
		this.rawData = data
		return request()
	}

	fun postRawJson(json: String): HttpResult {
		return postRawData("application/json;charset=utf-8", json.toByteArray(charsetUTF8))
	}

	fun postRawXML(xml: String): HttpResult {
		return postRawData("application/xml;charset=utf-8", xml.toByteArray(charsetUTF8))
	}

	fun download(saveto: File, progress: Progress?): HttpResult {
		return saveTo(saveto).progress(progress).get()
	}

}

//file, key, filename, mime都不能是空
class FileParam(val key: String,
				val file: File,
				var filename: String = file.name,
				var mime: String = "application/octet-stream"
) {


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
		//text/html;charset=utf-8
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

	val OK: Boolean get() = responseCode in 200..299

	val contentCharset: Charset?
		get() {
			if (contentType != null) {
				val ls: List<String> = contentType!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }
				for (item in ls) {
					val ss = item.trim()
					if (ss.startsWith("charset")) {
						val charset = ss.substringAfterLast('=', "")
						if (charset.length >= 2) {
							return Charset.forName(charset)
						}
					}
				}
			}
			return null
		}
	val responseText: String?
		get() {
			val bs = this.response ?: return null
			return String(bs, this.contentCharset ?: Charsets.UTF_8)
		}

	fun dump() {
		logd("Response:", this.url)
		logd("--status:", responseCode, responseMsg ?: "")
		val map = this.headerMap
		if (map != null) {
			for ((k, v) in map) {
				logd("--head:", k, "=", v)
			}
		}
		if (this.contentLength < 4096) {
			logd("--body:", this.responseText)
		}
	}

	fun needDecode(): HttpResult {
		this.needDecode = true
		return this
	}

	fun str(defCharset: Charset): String? {
		if (OK) {
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