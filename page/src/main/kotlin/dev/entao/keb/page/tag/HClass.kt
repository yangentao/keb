package dev.entao.keb.page.tag

data class HClass(val value: String) {
	infix operator fun rangeTo(c: HClass): String {
		val left = this.value
		val right = c.value
		if (left.isEmpty()) {
			return right
		}
		if (right.isEmpty()) {
			return left
		}
		return "$left $right"
	}


	infix operator fun rangeTo(right: String): String {
		val left = this.value
		if (left.isEmpty()) {
			return right
		}
		if (right.isEmpty()) {
			return left
		}
		return "$left $right"
	}

}


infix operator fun String.rangeTo(s: String): String {
	if (this.isEmpty()) {
		return s
	}
	if (s.isEmpty()) {
		return this
	}
	return "$this $s"
}

infix operator fun String.rangeTo(c: HClass): String {
	val s = c.value
	if (this.isEmpty()) {
		return s
	}
	if (s.isEmpty()) {
		return this
	}
	return "$this $s"
}

