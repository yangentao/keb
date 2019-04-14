package dev.entao.yson

import kotlin.reflect.KType


abstract class TypeTake<T> {

	val type: KType by lazy { this::class.supertypes.first().arguments.first().type!! }
}

fun escapeJson(s: String): String {
	var n = 0
	for (c in s) {
		if (c == '\\' || c == '\"') {
			n += 1
		}
	}
	if (n == 0) {
		return s
	}
	val sb = StringBuilder(s.length + n)
	for (c in s) {
		if (c == '\\' || c == '\"') {
			sb.append("\\")
		}
		sb.append(c)
	}
	return sb.toString()
}

