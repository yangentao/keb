package dev.entao.keb.core

import javax.servlet.FilterConfig

interface HttpSlice {
	fun onInit(filter: HttpFilter, config: FilterConfig) {}
	fun beforeRequest(context: HttpContext) {}

	fun acceptRouter(context: HttpContext, router: Router): Boolean {
		return true
	}

	fun afterRouter(context: HttpContext, r: Router) {}
	fun afterRequest(context: HttpContext) {}
	fun onDestory() {}
}