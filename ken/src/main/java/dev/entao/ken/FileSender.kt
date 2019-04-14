package dev.entao.ken

import dev.entao.kbase.Mimes
import dev.entao.ken.ex.header
import dev.entao.kbase.substringBetween
import java.io.File
import java.io.FileInputStream
import javax.servlet.ServletOutputStream

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

open class FileSender(val context: HttpContext) {

	private val os: ServletOutputStream by lazy { context.response.outputStream }

	var contentType: String
		get() = context.response.contentType ?: ""
		set(value) {
			context.response.contentType = value
		}

	var detectMime: Boolean = true

	fun contentType(type: String): FileSender {
		detectMime = false
		this.contentType = type
		return this
	}

	fun media(file: File, filename: String = file.name) {
		sendFile(0, file, filename)
	}

	fun attach(file: File, filename: String = file.name) {
		sendFile(1, file, filename)
	}

	fun inline_(file: File) {
		sendFile(2, file, "")
	}

	private fun sendFile(flag: Int, file: File, filename: String) {
		if (!file.exists() || !file.isFile) {
			context.abort(404, "文件没找到")
			return
		}

		val totalLength = file.length()
		if (detectMime) {
			context.response.contentType = Mimes.ofExt(file.extension)
		}
		if (flag == 1) {
			context.response.addHeader("Content-Disposition", "attachment;filename=$filename")
		} else if (flag == 2) {
			context.response.addHeader("Content-Disposition", "inline")
		} else {

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

	fun sendData(contentType: String, data: ByteArray) {
		val totalLength = data.size
		context.response.contentType = contentType

		context.response.addHeader("Content-Length", totalLength.toString())
		val rangeHead = findRange()
		if (rangeHead != null) {
			context.response.addHeader("Content-Range", "bytes 0-${totalLength - 1}/$totalLength")
			context.response.status = 206
		}
		os.write(data)
		os.close()
	}
	fun sendAttachFileData(contentType: String, filename:String, data: ByteArray) {
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