package dev.entao.yson

abstract class YsonValue {

	abstract fun yson(buf: StringBuilder)

	open fun yson(): String {
		val sb = StringBuilder(preferBufferSize())
		yson(sb)
		return sb.toString()
	}

	open fun preferBufferSize(): Int {
		return 64
	}

	override fun toString(): String {
		return yson()
	}

	val isCollection: Boolean get() = this is YsonObject || this is YsonArray


	companion object {
		fun from(value: Any?): YsonValue {
			return when (value) {
				null -> YsonNull.inst
				is YsonValue -> value
				is String -> YsonString(value)
				is Boolean -> YsonBool(value)
				is Number -> YsonNum(value)
				is ByteArray -> YsonBlob(value)
				else -> YsonString(value.toString())
			}
		}
	}
}