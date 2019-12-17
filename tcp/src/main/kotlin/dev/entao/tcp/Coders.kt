package dev.entao.tcp

import java.nio.ByteOrder
import java.nio.charset.Charset

interface FrameEncoder {
	val data: ByteArray
}

interface FrameDecoder<T> {
	fun decodeFrame(data: ByteArray): T
}

class BytesFrameDecoder : FrameDecoder<ByteArray> {
	override fun decodeFrame(data: ByteArray): ByteArray {
		return data
	}

}

class StringDecoder(private val charset: Charset = Charsets.UTF_8) : FrameDecoder<String> {
	override fun decodeFrame(data: ByteArray): String {
		return data.toString(charset)
	}

}


class LineEncoder(line: String, charset: Charset = Charsets.UTF_8) : FrameEncoder {
	override var data: ByteArray = line.toByteArray(charset) + byteArrayOf(10)
}

class FixLengthEncoder(data: ByteArray, length: Int) : FrameEncoder {
	override val data: ByteArray = ByteArray(length) {
		if (it < data.size) data[it] else 0
	}
}

class SizedEncoder(data: ByteArray, order: ByteOrder = ByteOrder.BIG_ENDIAN) : FrameEncoder {
	override val data: ByteArray = data.size.toByteArray(order) + data
}

class EndEdgeEncoder(data: ByteArray, endBytes: ByteArray) : FrameEncoder {
	override val data: ByteArray = data + endBytes
}

class EdgeEncoder(data: ByteArray, startBytes: ByteArray, endBytes: ByteArray) : FrameEncoder {
	override val data: ByteArray = startBytes + data + endBytes
}

fun Int.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
	val b0: Byte = ((this shr 24) and 0x00ff).toByte()
	val b1: Byte = ((this shr 16) and 0x00ff).toByte()
	val b2: Byte = ((this shr 8) and 0x00ff).toByte()
	val b3: Byte = (this and 0x00ff).toByte()
	return if (order == ByteOrder.BIG_ENDIAN) {
		byteArrayOf(b0, b1, b2, b3)
	} else {
		byteArrayOf(b3, b2, b1, b0)
	}
}