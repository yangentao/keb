package dev.entao.tcp

import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

class TcpWorker(val callback: TcpCallback) {

	val serviceLoop = TcpLoop(LineFrame())

	init {
		serviceLoop.callback = callback
	}

	val isOpen: Boolean
		get() = serviceLoop.isOpen

	@Synchronized
	fun start() {
		if (isOpen) {
			throw IllegalStateException("已存在是start状态")
		}
		serviceLoop.startLoop()
	}

	@Synchronized
	fun stop() {
		serviceLoop.stopLoop()
	}

	fun add(channel: SocketChannel) {
		serviceLoop.add(channel, SelectionKey.OP_READ)
	}


}