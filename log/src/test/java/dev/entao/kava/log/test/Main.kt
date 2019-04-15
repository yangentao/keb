package dev.entao.kava.log.test

import dev.entao.kava.log.Yog
import dev.entao.kava.log.YogDir
import dev.entao.kava.log.logd
import java.io.File

fun main() {
	val dir = File("/Users/entaoyang/yog")
	Yog.setPrinter(YogDir(dir, 3))
	Yog.setTagPrinter("yang", dir)
	logd("Hello", "Yang")
	logd("yang", "entao")
	Yog.flush()
	Yog.uninstall()
}