package dev.entao.yson

import dev.entao.kbase.userName
import dev.entao.kbase.isPublic
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

class ToYsonConfig {
	val map: HashMap<KClass<out Any>, IToYson> = HashMap(16)

	operator fun get(cls: KClass<*>): IToYson? {
		return map[cls]
	}

	companion object {
		val globalMap: HashMap<KClass<out Any>, IToYson> = HashMap(64)

		init {
			globalMap[Boolean::class] = BoolToYson
			globalMap[Byte::class] = ByteToYson
			globalMap[Short::class] = ShortToYson
			globalMap[Int::class] = IntToYson
			globalMap[Long::class] = LongToYson
			globalMap[BigInteger::class] = BigIntegerToYson
			globalMap[Float::class] = FloatToYson
			globalMap[Double::class] = DoubleToYson
			globalMap[BigDecimal::class] = BigDecimalToYson
			globalMap[Char::class] = CharToYson
			globalMap[String::class] = StringToYson
			globalMap[StringBuffer::class] = StringBufferToYson
			globalMap[StringBuilder::class] = StringBuilderToYson
			globalMap[ByteArray::class] = ByteArrayToYson
			globalMap[java.util.Date::class] = DateToYson
			globalMap[java.sql.Date::class] = SQLDateToYson
			globalMap[java.sql.Time::class] = SQLTimeToYson
			globalMap[java.sql.Timestamp::class] = SQLTimestampToYson
			globalMap[java.sql.Blob::class] = BlobToYson
		}

		fun get(cls: KClass<*>): IToYson? {
			return globalMap[cls]
		}

	}
}

object YsonEncoder {

	fun encode(m: Any?, config: ToYsonConfig?): YsonValue {
		if (m == null) {
			return YsonNull.inst
		}
		val toy = config?.get(m::class) ?: ToYsonConfig.get(m::class)
		if (toy != null) {
			return toy.toYsonValue(m)
		}
		return when (m) {
			is YsonValue -> m
			is CharArray -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is BooleanArray -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is ShortArray -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is IntArray -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is LongArray -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is FloatArray -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is DoubleArray -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is Array<*> -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}

			is Iterable<*> -> {
				val ya = YsonArray()
				m.mapTo(ya) { encode(it, config) }
				ya
			}
			is Map<*, *> -> {
				val yo = YsonObject(m.size)
				for ((k, v) in m) {
					yo[k.toString()] = encode(v, config)
				}
				yo
			}
			else -> {
				val ls = m::class.memberProperties.filter { it.isPublic && it is KMutableProperty1 && !it.isAbstract && !it.isConst }
				val yo = YsonObject(ls.size)
				ls.forEach { p ->
					val k = p.userName
					val v = p.getter.call(m)
					yo[k] = encode(v, config)
				}
				yo
			}
		}
	}
}