package dev.entao.kava.json

import dev.entao.kava.base.substr

class YsonError(msg: String = "YsonError") : Exception("Json解析错误, " + msg) {

	constructor(msg: String, text: String, pos: Int) : this(
			msg + ", " + text.substr(pos, 20)
	)
}