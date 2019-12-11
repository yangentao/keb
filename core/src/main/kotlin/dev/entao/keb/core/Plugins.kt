@file:Suppress("unused")

package dev.entao.keb.core

import dev.entao.kava.base.MIN
import dev.entao.kava.base.MyDate
import dev.entao.kava.log.Yog
import dev.entao.kava.log.fatal
import dev.entao.kava.log.logd
import dev.entao.kava.log.loge
import java.util.*
import javax.servlet.FilterConfig
import kotlin.reflect.KClass


interface HttpTimer {
	fun onHour(h: Int) {}
	fun onMinute(m: Int) {}
	fun onHttpTimer(dayOfMonth: Int, hour: Int, minute: Int)
}

object MethodAcceptor : HttpSlice {
	override fun acceptRouter(context: HttpContext, router: Router): Boolean {
		if (router.methods.isNotEmpty()) {
			if (context.request.method.toUpperCase() !in router.methods) {
				context.abort(400, "Method Error")
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

	override fun onInit(filter: HttpFilter, config: FilterConfig) {
		this.filter = filter
		timer?.cancel()
		timer = null
		val tm = Timer("everyMinute", true)
		val delay: Long = 1.MIN
		tm.scheduleAtFixedRate(tmtask, delay, delay)
		timer = tm
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
			val date = MyDate()
			val day = date.day
			val h = date.hour
			val minute = date.minute
			val timers = ArrayList<HttpTimer>(timerList)
			timers.forEach {
				try {
					filter?.onHttpTimer(day, h, minute)
					it.onHttpTimer(day, h, minute)
				} catch (ex: Exception) {
					ex.printStackTrace()
				}
			}

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
				val info = Router(uri, cls, f)
				addRouter(info)
			}
		}
	}

	fun addRouter(router: Router) {
		val u = router.uri.toLowerCase()
		if (routeMap.containsKey(u)) {
			val old = routeMap[u]
			fatal("已经存在对应的Route: ${old?.function} ", u, old.toString())
		}
		routeMap[u] = router
		logd("Add Router: ", u)
	}

}
