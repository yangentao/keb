package dev.entao.keb.page.tag


data class HAttr(val value: String) {
	infix fun to(s: String): HKeyValue {
		return this.value to s
	}

	infix fun to(cls: HClass): HKeyValue {
		return this.value to cls.value
	}
}

