@file:Suppress("unused")

package dev.entao.keb.core

import dev.entao.kava.base.MyDate
import dev.entao.kava.base.hasAnnotation
import dev.entao.kava.log.Yog
import dev.entao.kava.log.fatal
import dev.entao.kava.log.logd
import dev.entao.kava.log.loge
import dev.entao.keb.core.util.JWT
import java.util.*
import javax.servlet.FilterConfig
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

typealias HttpAction = KFunction<*>

interface HttpSlice {
	fun onConfig(filter: HttpFilter, config: FilterConfig) {}
	fun beforeAll(context: HttpContext) {}
	fun afterAll(context: HttpContext) {}
	fun beforeService(context: HttpContext, router: Router): Boolean {
		return true
	}

	fun afterService(context: HttpContext) {}
	fun onDestory() {}
}

interface PermAcceptor {
	fun prepare(context: HttpContext)
	fun accept(context: HttpContext, uri: String): Boolean
}

interface HttpTimer {
	fun onHour(h: Int) {}
	fun onMinute(m: Int) {}
}

object MethodAcceptor : HttpSlice {
	override fun beforeService(context: HttpContext, router: Router): Boolean {
		if (router.methods.isNotEmpty()) {
			if (context.request.method.toUpperCase() !in router.methods) {
				context.abort(400, "Method Error")
				return false
			}
		}
		return true
	}

}

object LoginCheckSlice : HttpSlice {

	override fun beforeService(context: HttpContext, router: Router): Boolean {
		if (router.function.hasAnnotation<NeedLogin>() || router.cls.hasAnnotation<NeedLogin>()) {
			if (!context.isLogined) {
				if (context.filter.loginUri.isNotEmpty()) {
					if (context.acceptHtml) {
						val u = Url(context.filter.loginUri)
						u.replace(Keb.BACK_URL, context.fullUrlOf(router.uri))
						context.redirect(u.build())
						return false
					}
				}
				context.abort(401)
				return false
			}
		}
		return true
	}
}

class TimerSlice : HttpSlice {


	private var filter: HttpFilter? = null
	private var timer: Timer? = null
	private val timerList = ArrayList<HttpTimer>()

	fun addTimer(t: HttpTimer) {
		if (t !in this.timerList) {
			this.timerList += t
		}
	}

	override fun onConfig(filter: HttpFilter, config: FilterConfig) {
		timer?.cancel()
		timer = null

		val tm = Timer("everyMinute", true)
		val delay: Long = 1000 * 60
		tm.scheduleAtFixedRate(tmtask, delay, delay)
		timer = tm
		this.filter = filter
	}

	override fun onDestory() {
		timer?.cancel()
		timer = null
		timerList.clear()
		filter = null
	}

	private val tmtask = object : TimerTask() {

		private var minN: Int = 0
		private var preHour = -1

		override fun run() {
			val timers = ArrayList<HttpTimer>(timerList)
			val h = MyDate().hour
			if (h != preHour) {
				preHour = h
				try {
					filter?.onHour(h)
				} catch (ex: Exception) {
					loge(ex)
				}
				for (ht in timers) {
					try {
						ht.onHour(h)
					} catch (ex: Exception) {
						loge(ex)
					}
				}
			}

			val n = minN++
			try {
				filter?.onMinute(n)
			} catch (ex: Exception) {
				loge(ex)
			}
			for (mt in timers) {
				try {
					mt.onMinute(n)
				} catch (ex: Exception) {
					loge(ex)
				}
			}

			try {
				Yog.flush()
			} catch (ex: Exception) {
			}
			filter?.cleanThreadLocals()
		}
	}

}

class HttpActionManager : HttpSlice {
	val allGroups = ArrayList<KClass<out HttpGroup>>()
	val routeMap = HashMap<String, Router>(32)

	lateinit var filter: HttpFilter

	override fun onDestory() {
		routeMap.clear()
		allGroups.clear()
	}

	fun find(context: HttpContext): Router? {
		return routeMap[context.currentUri]
	}

	fun addGroup(vararg clses: KClass<out HttpGroup>) {
		allGroups.addAll(clses)
		clses.forEach { cls ->
			for (f in cls.actionList) {
				val uri = filter.actionUri(f)
				val info = Router(uri, f)
				addRouter(info)
			}
		}
	}

	fun addRouter(router: Router) {
		val u = router.uri.toLowerCase()
		if (routeMap.containsKey(u)) {
			val old = routeMap[u]
			logd("AddRouter: ", u)
			fatal("已经存在对应的Route: ${old?.function} ", u, old.toString())
		}
		routeMap[u] = router
		logd("Add Router: ", u)
	}

}

//override fun onInit() {
//	addSlice(TokenSlice("99665588"))
//}
class TokenSlice(private val pwd: String) : HttpSlice {

	override fun beforeAll(context: HttpContext) {
		val a = context.request.header("Authorization") ?: return
		val b = a.substringAfter("Bearer ", "").trim()
		val token = if (b.isEmpty()) {
			context.request.param("access_token") ?: return
		} else {
			b
		}
		if (token.isEmpty()) {
			return
		}
		val j = JWT(pwd, token)
		context.tokenOK = j.OK
		context.accessToken = token
		if (j.OK) {
			context.tokenHeader = j.header
			context.tokenBody = j.body
		}
	}

	override fun beforeService(context: HttpContext, router: Router): Boolean {
		if (router.function.isNeedToken) {
			if (!context.tokenOK) {
				context.abort(401)
				return false
			}
		}
		return true
	}

	fun makeToken(body: String): String {
		return makeToken(body, """{"alg":"HS256","typ":"JWT"}""")
	}

	fun makeToken(body: String, header: String): String {
		return JWT.make(pwd, body, header)
	}
}
