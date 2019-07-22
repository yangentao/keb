package dev.entao.keb.core.util

import dev.entao.kava.base.Encrypt

class JWT(pwd: String, token: String) {
	var header: String = ""
	var body: String = ""
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
				header = Encrypt.B64.decode(h)
				body = Encrypt.B64.decode(d)
				sign = g
				OK = true
			}
		}
	}

	companion object {

		fun make(pwd: String, data: String, header: String = """{"alg":"HS256","typ":"JWT"}"""): String {
			val a = Encrypt.B64.encode(header)
			val b = Encrypt.B64.encode(data)
			val m = Encrypt.hmacSha256("$a.$b", pwd)
			return "$a.$b.$m"
		}
	}
}

//fun main() {
//	val a = JWT.make("123", YsonObject("""{"account":"entao"}"""))
//	println(a)
//	val b = JWT("123", a)
//	println(b.OK)
//	println(b.header)
//	println(b.data)
//}