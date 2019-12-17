@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.tcp

import java.nio.ByteOrder


abstract class BufferFrame {
	var data: ByteArray? = null

	abstract fun accept(buf: ByteArray): Int
}

private const val NL: Byte = 0
private const val LK: Byte = 123 // {
private const val RK: Byte = 125 // }
private const val QT: Byte = 34  // "
private const val ES: Byte = 92  // \
private const val SP: Byte = 32  // 空格
private const val CR: Byte = 13  // CR
private const val LF: Byte = 10  // LF
private const val TB: Byte = 9   // TAB
private val witeSpaces: Set<Byte> = setOf(SP, CR, LF, TB)

//只支持utf8 或 ascii
class JsonObjectFrame(val trim: Boolean = true) : BufferFrame() {
	override fun accept(buf: ByteArray): Int {
		if (buf.isEmpty()) {
			return 0
		}
		var fromIndex = 0
		if (trim) {
			while (buf[fromIndex] != NL && (buf[fromIndex] in witeSpaces)) {
				fromIndex += 1
			}
		}
		if (fromIndex >= buf.size) {
			return 0
		}
		if (buf[fromIndex] != LK) {
			return 0
		}
		var lkCount = 1
		var escaping = false
		var inString = false
		for (i in fromIndex + 1 until buf.size) {
			if (inString) {
				if (escaping) {
					escaping = false
				} else if (buf[i] == QT) {
					inString = false
				} else if (buf[i] == ES) {
					escaping = true
				}
				continue
			}
			when (buf[i]) {
				QT -> inString = true
				LK -> lkCount += 1
				RK -> lkCount -= 1
			}
			if (lkCount == 0) {
				data = buf.sliceArray(fromIndex..i)
				return i + 1
			}
		}
		return 0
	}

}


class FixLengthFrame(var length: Int) : BufferFrame() {

	override fun accept(buf: ByteArray): Int {
		if (buf.size >= length) {
			data = buf.sliceArray(0 until length)
			return length
		}
		return 0
	}
}

class EndEdgeFrame(private val end: ByteArray) : BufferFrame() {

	init {
		assert(end.isNotEmpty())
	}

	override fun accept(buf: ByteArray): Int {
		if (buf.size < end.size) {
			return 0
		}

		for (i in buf.indices) {
			if (i + end.size <= buf.size) {
				var acceptEnd = true
				for (k in end.indices) {
					if (end[k] != buf[i + k]) {
						acceptEnd = false
						break
					}
				}
				if (acceptEnd) {
					this.data = buf.sliceArray(0 until i)
					return i + end.size
				}
			}
		}
		return 0
	}
}

class EdgeFrame(private val start: ByteArray, private val end: ByteArray) : BufferFrame() {

	init {
		assert(start.isNotEmpty() && end.isNotEmpty())
	}

	override fun accept(buf: ByteArray): Int {
		if (buf.size < start.size + end.size) {
			return 0
		}
		for (i in start.indices) {
			if (buf[i] != start[i]) {
				return 0
			}
		}
		for (i in start.size until buf.size) {
			if (i + end.size <= buf.size) {
				var acceptEnd = true
				for (k in end.indices) {
					if (end[k] != buf[i + k]) {
						acceptEnd = false
						break
					}
				}
				if (acceptEnd) {
					this.data = buf.sliceArray(start.size until i)
					return i + end.size
				}
			}
		}
		return 0
	}
}

@Suppress("PrivatePropertyName")
class LineFrame : BufferFrame() {
	private val CR: Byte = 13
	private val LF: Byte = 10

	override fun accept(buf: ByteArray): Int {
		for (i in buf.indices) {
			if (buf[i] == CR || buf[i] == LF) {
				data = buf.sliceArray(0 until i)
				var k = i + 1
				while (k < buf.size) {
					if (buf[k] == CR || buf[k] == LF) {
						++k
					} else {
						break
					}
				}
				return k
			}
		}
		return 0
	}

}

class SizeFrame(private val byteSize: Int, private val order: ByteOrder = ByteOrder.BIG_ENDIAN) : BufferFrame() {

	init {
		assert(byteSize in 1..4)
	}

	private fun parseSize(buf: ByteArray): Int {
		var n = 0
		if (order == ByteOrder.BIG_ENDIAN) {
			for (i in 0 until byteSize) {
				val v = buf[i].toInt() and 0x00ff
				n = n shl 8
				n = n or v
			}
		} else {
			for (i in 0 until byteSize) {
				val v = (buf[i].toInt() and 0x00ff)
				n = n or (v shl (i * 8))
			}
		}
		return n
	}

	override fun accept(buf: ByteArray): Int {
		if (buf.size < byteSize) {
			return 0
		}
		val sz = parseSize(buf)
		if (sz == 0) {
			return byteSize
		}

		val n = byteSize + sz
		if (buf.size >= n) {
			data = buf.sliceArray(byteSize until n)
			return n
		}
		return 0
	}
}


fun testFixLength() {
	val buf = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
	val f = FixLengthFrame(3)
	val n = f.accept(buf)
	print(n)
	print(" ")
	println(f.data?.joinToString(",") { it.toString() })


}

fun testFrame(buf: ByteArray, f: BufferFrame) {
	val n = f.accept(buf)

	print(n)
	print(" ")
	println(f.data?.joinToString(",") { it.toString() })
}

fun testFrames() {
	val buf = byteArrayOf(0, 6, 3, 4, 5, 6, 7, 8, 9, 10, 13, 10, 13, 14)
	testFrame(buf, FixLengthFrame(3))
	testFrame(buf, EndEdgeFrame(byteArrayOf(4, 5)))
	testFrame(buf, EdgeFrame(byteArrayOf(1, 2), byteArrayOf(8, 9)))
	testFrame(buf, LineFrame())
//	testFrame(buf, SizeFrame(1))
	testFrame(buf, SizeFrame(2))
}

fun testJsonFrame() {
	val s = """
		{"name":"yang","addr":"{,hello"} {"age":22}
	""".trimIndent()
	val buf = s.toByteArray()
	val f = JsonObjectFrame()
	val n = f.accept(buf)
	print(buf.size)
	print(" ")
	println(n)
}


fun main() {
	testJsonFrame()
}