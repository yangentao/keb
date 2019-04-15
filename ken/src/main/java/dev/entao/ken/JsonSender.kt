@file:Suppress("unused")

package dev.entao.ken

import dev.entao.kava.json.YsonObject
import dev.entao.kava.json.YsonValue
import dev.entao.kava.json.ysonArray

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