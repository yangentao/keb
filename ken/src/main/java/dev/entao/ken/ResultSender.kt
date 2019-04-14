package dev.entao.ken

import dev.entao.ken.ex.contentTypeJson
import dev.entao.yog.logd
import dev.entao.yson.YsonArray
import dev.entao.yson.YsonObject
import dev.entao.yson.YsonObjectBuilder
import dev.entao.yson.ysonArray
import yet.servlet.Result

/**
 * Created by entaoyang@163.com on 2018/3/18.
 */

@Suppress("unused")
class ResultSender(val context: HttpContext) {

	init {
		context.response.contentTypeJson()
	}

	fun send(value: Result) {
		val s = value.toString()
		logd("Send: ", s)
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