package dev.entao.keb.core.render

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.substringBetween
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.header
import java.io.File
import java.io.FileInputStream
import javax.servlet.ServletOutputStream

open class FileSender(val context: HttpContext) {

	private val os: ServletOutputStream by lazy { context.response.outputStream }

	fun media(file: File, contentType: String = Mimes.ofFile(file.name)) {
		sendFile(false, file, contentType, "")
	}

	fun attach(file: File, contentType: String = Mimes.ofFile(file.name), filename: String = file.name) {
		sendFile(true, file, contentType, filename)
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