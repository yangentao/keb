package dev.entao.keb.am

import dev.entao.kava.sql.ConnLook
import dev.entao.keb.core.HttpFilter
import dev.entao.keb.core.TokenSlice
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

		addGroup(IndexGroup::class, ResGroup::class, ApkGroup::class)
		val a = TokenSlice("99665588")
		addSlice(a)
	}

}