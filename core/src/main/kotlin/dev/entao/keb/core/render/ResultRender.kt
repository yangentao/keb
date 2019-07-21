package dev.entao.keb.core.render

import dev.entao.kava.base.Mimes
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import dev.entao.kava.json.YsonObjectBuilder
import dev.entao.kava.json.ysonArray
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup

class Result {
	val jo = YsonObject()
	val ok: Boolean
		get() {
			return code == CODE_OK
		}
	var code: Int
		get() {
			return jo.int(CODE) ?: -1
		}
		set(value) {
			jo.int(CODE, value)
		}
	var msg: String
		get() {
			return jo.str(MSG) ?: ""
		}
		set(value) {
			jo.str(MSG, value)
		}

	var data: YsonObject?
		get() {
			return jo.obj(DATA)
		}
		set(value) {
			jo.obj(DATA, value)
		}

	var dataArray: YsonArray?
		get() {
			return jo.arr(DATA)
		}
		set(value) {
			jo.arr(DATA, value)
		}
	var dataInt: Int?
		get() {
			return jo.int(DATA)
		}
		set(value) {
			jo.int(DATA, value)
		}
	var dataLong: Long?
		get() {
			return jo.long(DATA)
		}
		set(value) {
			jo.long(DATA, value)
		}
	var dataDouble: Double?
		get() {
			return jo.real(DATA)
		}
		set(value) {
			jo.real(DATA, value)
		}
	var dataFloat: Float?
		get() {
			return jo.real(DATA)?.toFloat()
		}
		set(value) {
			jo.real(DATA, value?.toDouble())
		}
	var dataString: String?
		get() {
			return jo.str(DATA)
		}
		set(value) {
			jo.str(DATA, value)
		}

	override fun toString(): String {
		return jo.toString()
	}

	companion object {
		const val CODE_OK = 0
		const val MSG_OK = "操作成功"
		const val MSG_FAILED = "操作失败"

		var MSG = "msg"
		var CODE = "code"
		var DATA = "data"

		fun ok(msg: String = MSG_OK): Result {
			val r = Result()
			r.code = CODE_OK
			r.msg = msg
			return r
		}

		fun ok(msg: String, data: YsonObject): Result {
			val r = Result()
			r.code = CODE_OK
			r.msg = msg
			r.data = data
			return r
		}

		fun failed(code: Int, msg: String): Result {
			val r = Result()
			r.code = code
			r.msg = msg
			return r
		}
	}
}

@Suppress("unused")
class ResultRender(val context: HttpContext) {

	init {
		context.response.contentType = Mimes.JSON
	}

	fun send(value: Result) {
		val s = value.toString()
		context.response.writer.print(s)
	}

	fun result(block: Result.() -> Unit) {
		val r = Result()
		r.block()
		send(r)
	}

	fun ok() {
		val r = Result()
		r.code = Result.CODE_OK
		r.msg = Result.MSG_OK
		send(r)
	}

	fun ok(block: Result.() -> Unit) {
		val r = Result()
		r.code = Result.CODE_OK
		r.msg = Result.MSG_OK
		r.block()
		send(r)
	}

	fun ok(msg: String) {
		ok {
			this.msg = msg
		}
	}

	fun obj(block: YsonObjectBuilder.() -> Unit) {
		val jb = YsonObjectBuilder()
		jb.block()
		obj(jb.jo)
	}

	fun obj(data: YsonObject) {
		ok {
			this.data = data
		}
	}

	fun arr(array: YsonArray) {
		ok {
			this.dataArray = array
		}
	}

	fun <T> arr(list: Collection<T>, block: (T) -> Any?) {
		val ar = ysonArray(list, block)
		arr(ar)
	}

	fun int(n: Int) {
		ok {
			dataInt = n
		}
	}

	fun long(n: Long) {
		ok {
			dataLong = n
		}
	}

	fun double(n: Double) {
		ok {
			dataDouble = n
		}
	}

	fun float(n: Float) {
		ok {
			dataFloat = n
		}
	}

	fun str(s: String) {
		ok {
			dataString = s
		}
	}

	fun failed() {
		val r = Result()
		r.code = -1
		r.msg = Result.MSG_FAILED
		send(r)
	}

	fun failed(block: Result.() -> Unit) {
		val r = Result()
		r.code = -1
		r.msg = Result.MSG_FAILED
		r.block()
		send(r)
	}

	fun failed(msg: String) {
		failed {
			this.msg = msg
		}
	}

	fun failed(code: Int, msg: String) {
		failed {
			this.code = code
			this.msg = msg
		}
	}

}

