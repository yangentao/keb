package yet.util

import dev.entao.yog.Yog
import dev.entao.yog.loge
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Created by yet on 2015-11-20.
 */

@Suppress("UNUSED_PARAMETER")
private fun uncaughtException(thread: Thread, ex: Throwable) {
	ex.printStackTrace()
	loge(ex)
	Yog.flush()
}

object Task {
	private val es: ScheduledExecutorService = Executors.newScheduledThreadPool(4) {
		val t = Thread(it)
		t.isDaemon = true
		t.priority = Thread.NORM_PRIORITY
		t.setUncaughtExceptionHandler(::uncaughtException)
		t
	}

	fun back(callback: () -> Unit) {
		es.submit(callback)
	}

	fun afterMinutes(ms: Int, callback: () -> Unit) {
		es.schedule(callback, ms.toLong(), TimeUnit.MINUTES)
	}

	fun afterSeconds(secs: Int, callback: () -> Unit) {
		es.schedule(callback, secs.toLong(), TimeUnit.SECONDS)
	}
}

inline fun <R> sync(lock: Any, block: () -> R): R {
	return synchronized(lock, block)
}
