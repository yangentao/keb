package dev.entao.kava.json

class YsonNum(val data: Number) : YsonValue() {

	val dataInt: Int get() = data.toInt()

	val dataLong: Long get() = data.toLong()

	val dataFloat: Float get() = data.toFloat()
	val dataDouble: Double get() = data.toDouble()


	override fun yson(buf: StringBuilder) {
		buf.append(data.toString())
	}

	override fun equals(other: Any?): Boolean {
		if (other is YsonNum) {
			return other.data == data
		}
		return false
	}

	override fun hashCode(): Int {
		return data.hashCode()
	}

	override fun preferBufferSize(): Int {
		return 12
	}

	override fun toString(): String {
		return data.toString()
	}

}