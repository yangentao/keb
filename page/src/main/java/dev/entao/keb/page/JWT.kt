package dev.entao.keb.page

import dev.entao.kava.base.Encrypt
import dev.entao.kava.json.YsonObject

class JWT(pwd: String, token: String) {
	var header: YsonObject = YsonObject()
	var data: YsonObject = YsonObject()
	var sign: String = ""
	var OK: Boolean = false

	init {
		val ls = token.split('.')
		if (ls.size == 3) {
			val h = ls[0]
			val d = ls[1]
			val g = ls[2]
			val m = Encrypt.hmacSha256("$h.$d", pwd)
			if (m == g) {
				header = YsonObject(Encrypt.B64.decode(h))
				data = YsonObject(Encrypt.B64.decode(d))
				sign = g
				OK = true
			}
		}
	}

	companion object {

		fun make(pwd: String, data: YsonObject, header: String = """{"alg":"HS256","typ":"JWT"}"""): String {
			val a = Encrypt.B64.encode(header)
			val b = Encrypt.B64.encode(data.toString())
			val m = Encrypt.hmacSha256("$a.$b", pwd)
			return "$a.$b.$m"
		}
	}
}

fun main() {
	val a = JWT.make("123", YsonObject("""{"account":"entao"}"""))
	println(a)
	val b = JWT("123", a)
	println(b.OK)
	println(b.header)
	println(b.data)
}