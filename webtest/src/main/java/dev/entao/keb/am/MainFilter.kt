package dev.entao.keb.am

import dev.entao.kava.sql.ConnLook
import dev.entao.keb.core.HttpFilter
import javax.servlet.annotation.WebFilter

@WebFilter(urlPatterns = ["/*"])
class MainFilter : HttpFilter() {

	override fun cleanThreadLocals() {
		 ConnLook.cleanThreadConnections()
	}

	override fun onInit() {

		addGroup(IndexGroup::class)
	}

}