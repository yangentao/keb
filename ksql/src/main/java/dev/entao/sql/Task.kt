package dev.entao.sql

import dev.entao.kava.log.Yog
import dev.entao.kava.log.loge
import java.util.concurrent.*

/**
 * Created by yet on 2015-11-20.
 */

@Suppress("UNUSED_PARAMETER")
private fun uncaughtException(thread: Thread, ex: Throwable) {
	ex.printStackTrace()
	loge(ex)
	Yog.flush()
}

class ConnCleanRunnable(private val r: Runnable) : Runnable {
	override fun run() {
		try {
			r.run()
		} finally {
			ConnLook.removeThreadLocal()
		}
	}
}

object ConnThreadFactory : ThreadFactory {
	override fun newThread(r: Runnable): Thread {
		val t = Thread(ConnCleanRunnable(r))
		t.isDaemon = true
		t.priority = Thread.NORM_PRIORITY
		t.setUncaughtExceptionHandler(::uncaughtException)
		return t
	}

}

object Task {
	private val es: ScheduledExecutorService = Executors.newScheduledThreadPool(4, ConnThreadFactory)

	fun back(callback: () -> Unit): Future<*> {
		return es.submit(callback)
	}

	fun afterMinutes(ms: Int, callback: () -> Unit) : ScheduledFuture<*> {
		return es.schedule(callback, ms.toLong(), TimeUnit.MINUTES)
	}

	fun afterSeconds(secs: Int, callback: () -> Unit)  : ScheduledFuture<*>{
		return es.schedule(callback, secs.toLong(), TimeUnit.SECONDS)
	}
}

inline fun <R> sync(lock: Any, block: () -> R): R {
	return synchronized(lock, block)
}
