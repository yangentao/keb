@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.core

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.substringBetween
import dev.entao.kava.base.toXml
import dev.entao.kava.json.*
import dev.entao.kava.log.logd
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter
import javax.servlet.ServletOutputStream

open class HtmlSender(val context: HttpContext) {

	private val writer: PrintWriter by lazy { context.response.writer }

	init {
		context.response.contentTypeHtml()
	}

	fun print(s: String) {
		writer.print(s)
	}

	fun println(s: String) {
		writer.println(s)
	}

	fun text(s: String) {
		this.print(s)
	}
}

class JsonSender(val context: HttpContext) {

	init {
		context.response.contentTypeJson()
	}

	fun send(value: YsonValue) {
		context.response.writer.print(value.toString())
	}

	fun obj(block: YsonObject.() -> Unit) {
		val yo = YsonObject()
		yo.block()
		send(yo)
	}

	fun <T> arr(list: Collection<T>, block: (T) -> Any?) {
		val ar = ysonArray(list, block)
		send(ar)
	}
}

class XmlSender(val context: HttpContext) {

	init {
		context.response.contentTypeXml()
	}

	fun send(xml: String) {
		context.response.writer.print(xml)
	}

	fun send(ele: Element) {
		send(ele.toXml(false))
	}
}

@Suppress("unused")
open class TextSender(val context: HttpContext) {

	private val writer: PrintWriter by lazy { context.response.writer }

	init {
		context.response.contentTypePlain()
	}

	fun print(s: String) {
		writer.print(s)
	}

	fun println(s: String) {
		writer.println(s)
	}

	fun text(s: String) {
		print(s)
	}
}

class Result {

	val jo = YsonObject()

	val ok: Boolean
		get() {
			return code == CODE_OK
		}

	var code: Int
		get() {
			return jo.int(CODE) ?: -1
		}
		set(value) {
			jo.int(CODE, value)
		}
	var msg: String
		get() {
			return jo.str(MSG) ?: ""
		}
		set(value) {
			jo.str(MSG, value)
		}

	var data: YsonObject?
		get() {
			return jo.obj(DATA)
		}
		set(value) {
			jo.obj(DATA, value)
		}

	var dataArray: YsonArray?
		get() {
			return jo.arr(DATA)
		}
		set(value) {
			jo.arr(DATA, value)
		}
	var dataInt: Int?
		get() {
			return jo.int(DATA)
		}
		set(value) {
			jo.int(DATA, value)
		}
	var dataLong: Long?
		get() {
			return jo.long(DATA)
		}
		set(value) {
			jo.long(DATA, value)
		}
	var dataDouble: Double?
		get() {
			return jo.real(DATA)
		}
		set(value) {
			jo.real(DATA, value)
		}
	var dataFloat: Float?
		get() {
			return jo.real(DATA)?.toFloat()
		}
		set(value) {
			jo.real(DATA, value?.toDouble())
		}
	var dataString: String?
		get() {
			return jo.str(DATA)
		}
		set(value) {
			jo.str(DATA, value)
		}

	override fun toString(): String {
		return jo.toString()
	}

	companion object {
		const val CODE_OK = 0
		const val MSG_OK = "操作成功"
		const val MSG_FAILED = "操作失败"

		var MSG = "msg"
		var CODE = "code"
		var DATA = "data"

		fun ok(msg: String = MSG_OK): Result {
			val r = Result()
			r.code = CODE_OK
			r.msg = msg
			return r
		}

		fun ok(msg: String, data: YsonObject): Result {
			val r = Result()
			r.code = CODE_OK
			r.msg = msg
			r.data = data
			return r
		}

		fun failed(code: Int, msg: String): Result {
			val r = Result()
			r.code = code
			r.msg = msg
			return r
		}
	}
}

@Suppress("unused")
class ResultSender(val context: HttpContext) {

	init {
		context.response.contentTypeJson()
	}

	fun send(value: Result) {
		val s = value.toString()
		logd("Send: ", s)
		context.response.writer.print(s)
	}

	fun result(block: Result.() -> Unit) {
		val r = Result()
		r.block()
		send(r)
	}

	fun ok() {
		val r = Result()
		r.code = Result.CODE_OK
		r.msg = Result.MSG_OK
		send(r)
	}

	fun ok(block: Result.() -> Unit) {
		val r = Result()
		r.code = Result.CODE_OK
		r.msg = Result.MSG_OK
		r.block()
		send(r)
	}

	fun ok(msg: String) {
		ok {
			this.msg = msg
		}
	}

	fun obj(block: YsonObjectBuilder.() -> Unit) {
		val jb = YsonObjectBuilder()
		jb.block()
		obj(jb.jo)
	}

	fun obj(data: YsonObject) {
		ok {
			this.data = data
		}
	}

	fun arr(array: YsonArray) {
		ok {
			this.dataArray = array
		}
	}

	fun <T> arr(list: Collection<T>, block: (T) -> Any?) {
		val ar = ysonArray(list, block)
		arr(ar)
	}

	fun int(n: Int) {
		ok {
			dataInt = n
		}
	}

	fun long(n: Long) {
		ok {
			dataLong = n
		}
	}

	fun double(n: Double) {
		ok {
			dataDouble = n
		}
	}

	fun float(n: Float) {
		ok {
			dataFloat = n
		}
	}

	fun str(s: String) {
		ok {
			dataString = s
		}
	}

	fun failed() {
		val r = Result()
		r.code = -1
		r.msg = Result.MSG_FAILED
		send(r)
	}

	fun failed(block: Result.() -> Unit) {
		val r = Result()
		r.code = -1
		r.msg = Result.MSG_FAILED
		r.block()
		send(r)
	}

	fun failed(msg: String) {
		failed {
			this.msg = msg
		}
	}

	fun failed(code: Int, msg: String) {
		failed {
			this.code = code
			this.msg = msg
		}
	}

}

open class FileSender(val context: HttpContext) {

	private val os: ServletOutputStream by lazy { context.response.outputStream }

	fun media(file: File, contentType: String = Mimes.ofFile(file.name)) {
		sendFile(false, file, contentType, "")
	}

	fun attach(file: File, contentType: String = Mimes.ofFile(file.name), filename: String = file.name) {
		sendFile(true, file, filename, contentType)
	}

	private fun sendFile(isAttach: Boolean, file: File, contentType: String, filename: String) {
		if (!file.exists() || !file.isFile) {
			return context.abort(404, "文件没找到")
		}
		val totalLength = file.length()
		context.response.contentType = contentType
		if (isAttach) {
			context.response.addHeader("Content-Disposition", "attachment;filename=$filename")
		}
		context.response.addHeader("Content-Length", totalLength.toString())
		val rangeHead = findRange()
		if (rangeHead != null) {
			if (rangeHead.second >= 0) {
				context.response.addHeader("Content-Range", "bytes ${rangeHead.first}-${rangeHead.second}/$totalLength")
			} else {
				context.response.addHeader("Content-Range", "bytes ${rangeHead.first}-${totalLength - 1}/$totalLength")
			}
			context.response.status = 206
			file.inputStream().use {
				outRange(rangeHead.first, rangeHead.second, it, os)
			}
		} else {
			file.inputStream().use {
				it.copyTo(os)
			}
		}
		os.close()
	}

	private fun outRange(start: Int, end: Int, fis: FileInputStream, os: ServletOutputStream) {
		if (start > 0) {
			fis.skip(start.toLong())
		}
		if (end == start) {
			val b = fis.read()
			os.write(b)
			return
		}
		//-1
		if (end < start) {
			fis.copyTo(os)
			return
		}
		val total = end - start + 1
		var readed = 0
		val buf = ByteArray(4096)
		do {
			val n = fis.read(buf)
			if (n < 0) {
				return
			}
			if (readed + n <= total) {
				os.write(buf, 0, n)
				readed += n
				continue
			}
			if (readed < total) {
				os.write(buf, 0, total - readed)
				return
			}
			return
		} while (true)
	}

	//Range: bytes=0-801
	private fun findRange(): Pair<Int, Int>? {
		val range = context.request.header("Range") ?: return null
		val startStr = range.substringBetween('=', '-')?.trim() ?: return null
		val endStr = range.substringAfter('_', "").trim()
		val startBytes = startStr.toIntOrNull() ?: return null
		val endBytes = if (endStr.isEmpty()) -1 else endStr.toInt()
		return Pair(startBytes, endBytes)
	}

	fun sendData(data: ByteArray, contentType: String) {
		val totalLength = data.size
		val r = context.response
		r.contentType = contentType
		r.addHeader("Content-Length", totalLength.toString())
		val rangeHead = findRange()
		if (rangeHead != null) {
			context.response.addHeader("Content-Range", "bytes 0-${totalLength - 1}/$totalLength")
			context.response.status = 206
		}
		os.write(data)
		os.close()
	}

	fun sendDataAttach(data: ByteArray, filename: String, contentType: String = Mimes.ofFile(filename)) {
		val totalLength = data.size
		context.response.contentType = contentType
		context.response.addHeader("Content-Disposition", "attachment;filename=$filename")
		context.response.addHeader("Content-Length", totalLength.toString())
		val rangeHead = findRange()
		if (rangeHead != null) {
			context.response.addHeader("Content-Range", "bytes 0-${totalLength - 1}/$totalLength")
			context.response.status = 206
		}
		os.write(data)
		os.close()
	}
}