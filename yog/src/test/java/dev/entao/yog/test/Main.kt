package dev.entao.yog.test

import dev.entao.yog.Yog
import dev.entao.yog.YogDir
import dev.entao.yog.logd
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