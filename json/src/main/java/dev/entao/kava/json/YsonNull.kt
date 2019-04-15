package dev.entao.kava.json

class YsonNull private constructor() : YsonValue() {

	override fun yson(buf: StringBuilder) {
		buf.append("null")
	}

	override fun equals(other: Any?): Boolean {
		return other is YsonNull
	}

	override fun preferBufferSize(): Int {
		return 8
	}

	override fun hashCode(): Int {
		return 1000
	}


	override fun toString(): String {
		return "null"
	}

	companion object {
		val inst: YsonNull = YsonNull()
	}
}