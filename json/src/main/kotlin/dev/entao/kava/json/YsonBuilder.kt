package dev.entao.kava.json

import dev.entao.kava.base.userName
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2016-12-29.
 */



fun ysonArray(values: Collection<Any?>): YsonArray {
	val arr = YsonArray()
	for (v in values) {
		arr.addAny(v)
	}
	return arr
}

fun ysonArray(vararg values: Any): YsonArray {
	val arr = YsonArray()
	for (v in values) {
		arr.addAny(v)
	}
	return arr
}

fun <T> ysonArray(values: Collection<T>, block: (T) -> Any?): YsonArray {
	val arr = YsonArray()
	for (v in values) {
		arr.addAny(block(v))
	}
	return arr
}