package dev.entao.kava.base

import dev.entao.kava.log.Yog
import dev.entao.kava.log.loge
import java.util.concurrent.*


@Suppress("UNUSED_PARAMETER")
private fun uncaughtException(thread: Thread, ex: Throwable) {
    ex.printStackTrace()
    loge(ex)
    Yog.flush()
}

typealias CleanBlock = () -> Unit

object Task {
    private val cleanMap = LinkedHashMap<String, CleanBlock>()
    private val es: ScheduledExecutorService = Executors.newScheduledThreadPool(2) {
        val t = Thread(CleanRunnable(it))
        t.isDaemon = true
        t.priority = Thread.NORM_PRIORITY
        t.setUncaughtExceptionHandler(::uncaughtException)
        t
    }

    private class CleanRunnable(private val r: Runnable) : Runnable {
        override fun run() {
            try {
                r.run()
            } catch (ex: Throwable) {
                Yog.e(ex)
            } finally {
                if (cleanMap.isNotEmpty()) {
                    val ls = ArrayList<CleanBlock>(Task.cleanMap.values)
                    for (b in ls) {
                        try {
                            b()
                        } catch (ex: Throwable) {
                            Yog.e(ex)
                        }
                    }
                }
            }
        }
    }

    private class CleanCallback(private val callback: () -> Unit) : Runnable {
        override fun run() {
            try {
                callback()
            } catch (ex: Throwable) {
                Yog.e(ex)
            } finally {
                if (cleanMap.isNotEmpty()) {
                    val ls = ArrayList<CleanBlock>(Task.cleanMap.values)
                    for (b in ls) {
                        try {
                            b.invoke()
                        } catch (ex: Throwable) {
                            Yog.e(ex)
                        }
                    }
                }
            }
        }
    }

    val pool: ScheduledExecutorService get() = this.es

    fun setCleanBlock(name: String, block: CleanBlock) {
        cleanMap[name] = block
    }


    fun back(callback: () -> Unit): Future<*> {
        return es.submit(CleanCallback(callback))
    }

    fun afterMinutes(ms: Int, callback: () -> Unit): ScheduledFuture<*> {
        return es.schedule(CleanCallback(callback), ms.toLong(), TimeUnit.MINUTES)
    }

    fun afterSeconds(secs: Int, callback: () -> Unit): ScheduledFuture<*> {
        return es.schedule(CleanCallback(callback), secs.toLong(), TimeUnit.SECONDS)
    }
}

inline fun <R> sync(lock: Any, block: () -> R): R {
    return synchronized(lock, block)
}
