package dev.entao.keb.core

import dev.entao.kava.base.MyDate
import dev.entao.kava.log.Yog
import dev.entao.kava.log.loge
import dev.entao.kava.sql.ConnLook
import java.util.*
import javax.servlet.FilterConfig

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
			ConnLook.removeThreadLocal()
		}
	}

}