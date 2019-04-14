package dev.entao.ken.client

import dev.entao.yog.log
import dev.entao.yog.logd
import dev.entao.yog.loge
import dev.entao.kbase.Progress
import dev.entao.kbase.SizeStream
import dev.entao.kbase.closeSafe
import dev.entao.kbase.copyStream
import java.io.*
import java.net.*
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
    private val BOUNDARY_START = "--" + BOUNDARY + "\r\n"
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

    init {
        userAgent("android")
        accept("application/json,text/plain,text/html,*/*")
        acceptLanguage("zh-CN,en-US;q=0.8,en;q=0.6")
        headerMap.put("Accept-Charset", "UTF-8,*")
        headerMap.put("Connection", "close")
        headerMap.put("Charset", UTF8)
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
            headerMap.put(k, v)
        }
        return this
    }

    fun header(key: String, value: String): Http {
        headerMap.put(key, value)
        return this
    }

    fun headers(map: Map<String, String>): Http {
        headerMap.putAll(map)
        return this;
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
        headerMap.put("Accept", accept)
        return this
    }

    fun acceptLanguage(acceptLanguage: String): Http {
        headerMap.put("Accept-Language", acceptLanguage)
        return this
    }

    fun auth(user: String, pwd: String): Http {
        val usernamePassword = user + ":" + pwd
        val encodedUsernamePassword = Base64.getUrlEncoder().encodeToString(usernamePassword.toByteArray(charsetUTF8))
        headerMap.put("Authorization", "Basic " + encodedUsernamePassword)
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
        argMap.put(key, value)
        return this
    }

    fun arg(key: String, value: Long): Http {
        argMap.put(key, "" + value)
        return this
    }


    fun args(vararg args: Pair<String, String>): Http {
        for ((k, v) in args) {
            argMap.put(k, v)
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
        headerMap.put("Range", "bytes=$from-$to")
        return this
    }

    fun range(from: Int): Http {
        headerMap.put("Range", "bytes=$from-")
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
                val name = URLEncoder.encode(e.key, UTF8)
                val value = URLEncoder.encode(e.value, UTF8)
                if (sb.length > 0) {
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
        if (sArgs.length > 0) {
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
        val result = HttpResult()
        result.responseCode = connection.responseCode
        result.responseMsg = connection.responseMessage
        result.contentType = connection.contentType
        result.headerMap = connection.headerFields
        val total = connection.contentLength
        result.contentLength = total

	    logd("--HttpStatus:", result.responseCode, result.responseMsg ?: "")

        var os: OutputStream = if (this.saveToFile != null) {
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
                    if (s.length > 0) {
                        write(os, s)
                    }
                }
                HttpMethod.POST_MULTIPART -> sendMultipart(os)
                HttpMethod.POST_RAW_DATA -> os.write(rawData!!)
                else ->{}
            }
            os.flush()
        } finally {
            os.closeSafe()
        }
    }

    private fun request(): HttpResult {
        var connection: HttpURLConnection? = null
        try {
	        log("Http Request:", url)
            for ((k, v) in argMap) {
	            log("--arg:", k, "=", v)
            }
            for (fp in fileList) {
	            log("--file:", fp)
            }
            if (method == HttpMethod.GET || method == HttpMethod.POST_RAW_DATA) {
                connection = URL(buildGetUrl()).openConnection() as HttpURLConnection
            } else {
                connection = URL(url).openConnection() as HttpURLConnection
            }

            preConnect(connection)
            connection.connect()
            onSend(connection)
            return onResponse(connection)
        } catch (ex: Exception) {
            ex.printStackTrace()
	        loge(ex)
            val result = HttpResult()
            result.exception = ex
            return result
        } finally {
            if (connection != null) {
                connection.disconnect()
            }
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
        return postRawData("text/json;charset=utf-8", json.toByteArray(charsetUTF8))
    }

    fun postRawXML(xml: String): HttpResult {
        return postRawData("text/xml;charset=utf-8", xml.toByteArray(charsetUTF8))
    }

    fun download(saveto: File, progress: Progress?): HttpResult {
        return saveTo(saveto).progress(progress).get()
    }

}
