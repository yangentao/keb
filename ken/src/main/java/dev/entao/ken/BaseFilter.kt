package dev.entao.ken

import dev.entao.kava.log.logd
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by entaoyang@163.com on 2017/4/6.
 */

abstract class BaseFilter : Filter {

	lateinit var filterConfig: FilterConfig

	override fun init(filterConfig: FilterConfig) {
		this.filterConfig = filterConfig
	}

	final override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
		try {
			if (request is HttpServletRequest && response is HttpServletResponse) {
				request.characterEncoding = "UTF-8"
				response.characterEncoding = "UTF-8"
				if (!doHttpFilter(request, response)) {
					chain.doFilter(request, response)
				}
			} else {
				chain.doFilter(request, response)
			}
		} catch (ex: Exception) {
			logd(ex)
			throw  ex
		}
	}

	override fun destroy() {
	}

	abstract fun doHttpFilter(request: HttpServletRequest, response: HttpServletResponse): Boolean

}