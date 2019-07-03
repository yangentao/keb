package dev.entao.keb.core

import javax.servlet.FilterConfig

class AccepterManager : HttpSlice {


	private val acceptList = ArrayList<Acceptor>(8)

	override fun onConfig(filter: HttpFilter, config: FilterConfig) {
		acceptList.clear()
	}

	override fun beforeService(c: HttpContext, r: Router): Boolean {
		for (a in this.acceptList) {
			if (!a.accept(c, r)) {
				return false
			}
		}
		return true
	}

	override fun onDestory() {
		acceptList.clear()
	}

	fun addAcceptor(a: Acceptor) {
		if (a !in this.acceptList) {
			this.acceptList += a
		}
	}
}