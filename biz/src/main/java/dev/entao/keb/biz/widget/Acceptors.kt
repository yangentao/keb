package dev.entao.keb.biz.widget

import dev.entao.keb.biz.model.Ip
import dev.entao.keb.core.Router

object IpAcceptor : dev.entao.keb.core.Acceptor {
	override fun accept(context: dev.entao.keb.core.HttpContext, router: Router): Boolean {
		Ip.save(context)
		return true
	}
}