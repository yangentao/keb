package dev.entao.keb.biz.widget

import dev.entao.keb.biz.model.Ip
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpSlice
import dev.entao.keb.core.Router

object IpAcceptor : HttpSlice {
	override fun beforeService(context: HttpContext, router: Router): Boolean {
		Ip.save(context)
		return true
	}
}