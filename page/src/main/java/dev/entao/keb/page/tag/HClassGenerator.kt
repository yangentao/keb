@file:Suppress("ObjectPropertyName", "unused")

package dev.entao.keb.page.tag

import dev.entao.kava.base.replaceChars
import java.io.File
import java.io.FileOutputStream
import java.util.*


fun _formatClassName(a: String): String {
	if (':' in a || ';' in a || '(' in a || '[' in a) {
		return ""
	}
	if (a.lastIndexOf('.') != 0) {
		return ""
	}
	return a.substring(1)
}

fun makeClassFileByBootstrapCSS(inFile: File): TreeSet<String> {
	val set = TreeSet<String>()
	val lines = inFile.readLines()
	println("LineCount: " + lines.size)
	for (line in lines) {
		val sr = ".sr-only" in line
		if (sr) {
			println(line)
		}
		val cs = line.split(',', ' ', ':', '(', ')', ';', ']')
		for (a in cs) {
			val b = a.trim()
			if (b.startsWith('.')) {
				val c = _formatClassName(b)
				if (sr) {
					println("Formated: $c ")
				}
				if (c.isNotEmpty()) {

					set.add(c)
				}
			}
		}
	}
	println("SetSize: ${set.size}")
	return set


}

fun main() {
	val dir = File("/Users/yangentao/Downloads/")
	val outFile = File(dir, "out.txt")
	val outStream = outFile.outputStream()
	val set = makeClassFileByBootstrapCSS(File(dir, "bootstrap.css"))

	val ls = set.map { s ->
		val v = s.replaceChars('-' to "_")
		"""val _$v = HClass("$s")"""
	}

	val total = ls.joinToString("\n")
	outStream.write(total.toByteArray())
	outStream.close()
}