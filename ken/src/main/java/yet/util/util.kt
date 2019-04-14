package yet.util

import java.text.Collator
import java.util.*

/**
 * Created by entaoyang@163.com on 2016/12/21.
 */

class Parent<T>(val data: T) {

	val children: ArrayList<T> = ArrayList()

	fun add(a: T) {
		children.add(a)
	}
}

class Node<T>(val data: T) {

	val children: ArrayList<Node<T>> = ArrayList()

	fun addChild(a: T) {
		children.add(Node(a))
	}

	fun addNode(n: Node<T>) {
		children.add(n)
	}
}

val chinaCollator: Collator by lazy {
	Collator.getInstance(Locale.CHINESE)
}

class ChinaComparator<T>(val block: (T) -> String?) : Comparator<T> {
	override fun compare(o1: T, o2: T): Int {
		if (o1 === o2) {
			return 0
		}
		if (o1 == null) {
			return -1
		}
		if (o2 == null) {
			return 1
		}
		val s1 = block(o1)
		val s2 = block(o2)
		if (s1 == s2) {
			return 0
		}
		if (s1 == null) {
			return -1
		}
		if (s2 == null) {
			return 1
		}
		return chinaCollator.compare(s1, s2)
	}

}

inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
	var closed = false
	try {
		return block(this)
	} catch (e: Exception) {
		closed = true
		try {
			this?.close()
		} catch (closeException: Exception) {
		}
		throw e
	} finally {
		if (!closed) {
			this?.close()
		}
	}
}

fun MutableList<String>.chinaSort() {
	this.sortWith(yet.util.chinaCollator)
}

fun Iterable<String>.chinaSorted(): List<String> {
	return this.sortedWith(yet.util.chinaCollator)
}

fun <T> MutableList<T>.chinaSortBy(block: (T) -> String?) {
	val cmp = ChinaComparator<T>(block)
	this.sortWith(cmp)
}

fun <T> Iterable<T>.chinaSortedBy(block: (T) -> String?): List<T> {
	val cmp = ChinaComparator<T>(block)
	return this.sortedWith(cmp)
}

fun <T> inSet(value: T, vararg ts: T): Boolean {
	for (v in ts) {
		if (value == v) {
			return true
		}
	}
	return false
}

fun UUID.random(): String = UUID.randomUUID().toString()

fun makeUUID(): String = UUID.randomUUID().toString()