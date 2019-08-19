package dev.entao.speak

import dev.entao.kava.sql.ConnLook
import dev.entao.keb.core.HttpFilter
import dev.entao.keb.core.TokenSlice
import dev.entao.keb.page.modules.ResGroup
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebFilter

@MultipartConfig
@WebFilter(urlPatterns = ["/s/*"])
class MainFilter : HttpFilter() {

	override fun cleanThreadLocals() {
		ConnLook.cleanThreadConnections()
	}

	override fun onInit() {
		addSlice(TokenSlice("speak123"))
		addGroup(IndexGroup::class, ResGroup::class, ApkGroup::class)
	}

}