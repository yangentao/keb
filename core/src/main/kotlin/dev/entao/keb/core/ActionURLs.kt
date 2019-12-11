package dev.entao.keb.core

import dev.entao.kava.base.firstParamName
import dev.entao.kava.base.urlEncoded
import dev.entao.kava.base.userName
import kotlin.math.min
import kotlin.reflect.full.valueParameters

class ActionURL(val action: HttpAction) {
	val map = LinkedHashMap<String, String>()

	fun toURL(context: HttpContext): String {
		val s = context.filter.actionUri(action)
		if (map.isEmpty()) {
			return s
		}
		val b = map.map { it.key + "=" + it.value.urlEncoded }.joinToString("&")
		return "$s?$b"
	}

	fun fullURL(context: HttpContext): String {
		return context.fullUrlOf(this.toURL(context))
	}

}

operator fun HttpAction.unaryPlus(): ActionURL {
	return ActionURL(this)
}

operator fun ActionURL.plus(v: String): ActionURL {
	val argName = action.firstParamName
	if (argName != null) {
		map[argName] = v
	} else {
		//throw IllegalArgumentException("函数 $action 无参数")
	}
	return this
}

operator fun ActionURL.plus(v: Number): ActionURL {
	return this + v.toString()
}


operator fun ActionURL.plus(p: Pair<String, Any?>): ActionURL {
	map[p.first] = p.second?.toString() ?: ""
	return this
}

operator fun ActionURL.plus(vs: List<Any?>): ActionURL {
	val nameList = action.valueParameters.map { it.userName }
	val minSize: Int = min(vs.size, nameList.size)
	for (i in 0 until minSize) {
		map[nameList[i]] = vs[i]?.toString() ?: ""
	}
	return this
}


operator fun HttpAction.plus(v: String): ActionURL {
	return +this + v
//	return ActionURL(this) + v
}

operator fun HttpAction.plus(v: Number): ActionURL {
	return ActionURL(this) + v
}


operator fun HttpAction.plus(p: Pair<String, Any?>): ActionURL {
	return ActionURL(this) + p
}

operator fun HttpAction.plus(vs: List<Any?>): ActionURL {
	return ActionURL(this) + vs
}

