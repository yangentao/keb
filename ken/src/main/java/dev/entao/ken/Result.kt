@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.ken

import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject

/**
 * Created by entaoyang@163.com on 2017/4/4.
 */

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