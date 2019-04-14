package dev.entao.yson

import dev.entao.kbase.MyDate
import java.math.BigDecimal
import java.math.BigInteger

interface IFromYson {
	fun fromYsonValue(yv: YsonValue): Any?
}

object BoolFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonBool) {
			return yv.data
		}
		return null
	}
}

object ByteFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return yv.data.toByte()
		}
		return null
	}
}

object ShortFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return yv.data.toShort()
		}
		return null
	}
}

object IntFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return yv.data.toInt()
		}
		return null
	}
}

object LongFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return yv.data
		}
		return null
	}
}

object BigIntegerFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return BigInteger(yv.data.toString())
		}
		return null
	}
}

object FloatFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return yv.data.toFloat()
		}
		return null
	}
}

object DoubleFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return yv.data
		}
		return null
	}
}

object BigDecimalFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonNum) {
			return BigDecimal(yv.data.toDouble())
		}
		return null
	}
}

object CharFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			return yv.data.firstOrNull()
		}
		return null
	}
}

object StringFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			return yv.data
		} else if (yv is YsonBlob) {
			return yv.encoded
		}
		return null
	}
}

//

object StringBufferFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			return StringBuffer(yv.data)
		} else if (yv is YsonBlob) {
			return StringBuffer(yv.encoded)
		}
		return null
	}
}

object StringBuilderFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			return StringBuilder(yv.data)
		} else if (yv is YsonBlob) {
			return StringBuilder(yv.encoded)
		}
		return null
	}
}

object DateFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			val d = MyDate.parseDateTime(yv.data) ?: return null
			return java.util.Date(d.time)
		} else if (yv is YsonNum) {
			return java.util.Date(yv.data.toLong())
		}
		return null
	}
}

object SQLDateFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			val d = MyDate.parseDate(yv.data) ?: return null
			return java.sql.Date(d.time)
		} else if (yv is YsonNum) {
			return java.sql.Date(yv.data.toLong())
		}
		return null
	}
}

object SQLTimeFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			val d = MyDate.parseTime(yv.data) ?: return null
			return java.sql.Time(d.time)
		} else if (yv is YsonNum) {
			return java.sql.Time(yv.data.toLong())
		}
		return null
	}
}

object SQLTimestampFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonString) {
			val d = MyDate.parseDateTime(yv.data) ?: return null
			return java.sql.Timestamp(d.time)
		} else if (yv is YsonNum) {
			return java.sql.Timestamp(yv.data.toLong())
		}
		return null
	}
}

object BoolArrayFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		return (yv as? YsonArray)?.toBoolArray()
	}
}

object ByteArrayFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		if (yv is YsonBlob) {
			return yv.data
		} else if (yv is YsonString) {
			return YsonBlob.decode(yv.data)
		}
		return (yv as? YsonArray)?.toByteArray()
	}
}

object ShortArrayFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		return (yv as? YsonArray)?.toShortArray()
	}
}

object IntArrayFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		return (yv as? YsonArray)?.toIntArray()
	}
}

object LongArrayFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		return (yv as? YsonArray)?.toLongArray()
	}
}

object FloatArrayFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		return (yv as? YsonArray)?.toFloatArray()
	}
}

object DoubleArrayFromYson : IFromYson {
	override fun fromYsonValue(yv: YsonValue): Any? {
		return (yv as? YsonArray)?.toDoubleArray()
	}
}
