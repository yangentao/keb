package dev.entao.kava.base

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream




class SizeStream : OutputStream() {
	var size = 0
		private set

	@Throws(IOException::class)
	override fun write(oneByte: Int) {
		++size
	}

	fun incSize(size: Int) {
		this.size += size
	}

}

fun AutoCloseable.closeSafe() {
	try {
		this.close()
	} catch (ex: Throwable) {
		ex.printStackTrace()
	}
}


@Throws(IOException::class)
fun copyStream(input: InputStream, closeIs: Boolean, os: OutputStream, closeOs: Boolean, total: Int, progress: Progress?) {
	try {
		progress?.onStart(total)

		val buf = ByteArray(4096)
		var pre = System.currentTimeMillis()
		var recv = 0

		var n = input.read(buf)
		while (n != -1) {
			os.write(buf, 0, n)
			recv += n
			if (progress != null) {
				val curr = System.currentTimeMillis()
				if (curr - pre > PROGRESS_DELAY) {
					pre = curr
					progress.onProgress(recv, total, if (total > 0) recv * 100 / total else 0)
				}
			}
			n = input.read(buf)
		}
		os.flush()
		progress?.onProgress(recv, total, if (total > 0) recv * 100 / total else 0)
	} finally {
		if (closeIs) {
			input.closeSafe()
		}
		if (closeOs) {
			os.closeSafe()
		}
		progress?.onFinish()
	}
}