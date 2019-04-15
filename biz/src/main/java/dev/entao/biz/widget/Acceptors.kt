package dev.entao.biz.widget

import dev.entao.ken.Acceptor
import dev.entao.ken.HttpContext
import dev.entao.ken.Router
import dev.entao.biz.model.Ip

object IpAcceptor : Acceptor {
	override fun accept(context: HttpContext, router: Router): Boolean {
		Ip.save(context)
		return true
	}
}