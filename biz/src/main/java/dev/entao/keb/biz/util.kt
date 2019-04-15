package dev.entao.keb.biz

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
