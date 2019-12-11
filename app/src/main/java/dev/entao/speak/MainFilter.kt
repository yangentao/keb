package dev.entao.speak

import dev.entao.kava.sql.ConnLook
import dev.entao.keb.core.HttpFilter
import dev.entao.keb.page.modules.ResGroup
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebFilter

@MultipartConfig
@WebFilter(urlPatterns = ["/*"])
class MainFilter : HttpFilter() {

	override fun cleanThreadLocals() {
		ConnLook.cleanThreadConnections()
	}

	override fun onInit() {
		addGroup(ResGroup::class, IndexGroup::class)
	}

}