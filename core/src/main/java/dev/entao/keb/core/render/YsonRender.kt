package dev.entao.keb.core.render

import dev.entao.kava.base.Mimes
import dev.entao.kava.json.YsonObject
import dev.entao.kava.json.YsonValue
import dev.entao.kava.json.ysonArray
import dev.entao.keb.core.HttpContext

class YsonRender(val context: HttpContext) {

	init {
		context.response.contentType = Mimes.JSON
	}

	fun write(value: YsonValue) {
		context.response.writer.print(value.toString())
	}

	fun writeObject(block: YsonObject.() -> Unit) {
		val yo = YsonObject()
		yo.block()
		write(yo)
	}

	fun <T> writeArray(list: Collection<T>, block: (T) -> Any?) {
		val ar = ysonArray(list, block)
		write(ar)
	}
}