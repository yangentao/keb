@file:Suppress("unused")

package dev.entao.ken

import dev.entao.ken.ex.contentTypeJson
import dev.entao.yson.YsonObject
import dev.entao.yson.YsonValue
import dev.entao.yson.ysonArray

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

class JsonSender(val context: HttpContext) {

	init {
		context.response.contentTypeJson()
	}

	fun send(value: YsonValue) {
		context.response.writer.print(value.toString())
	}


	fun obj(block: YsonObject.() -> Unit) {
		val yo = YsonObject()
		yo.block()
		send(yo)
	}


	fun <T> arr(list: Collection<T>, block: (T) -> Any?) {
		val ar = ysonArray(list, block)
		send(ar)
	}

}