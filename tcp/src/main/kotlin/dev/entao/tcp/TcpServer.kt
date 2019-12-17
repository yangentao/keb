@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.tcp

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel


class TcpServer(workCallback: TcpCallback) : TcpCallback {
	private var keyLoop: TcpLoop = TcpLoop(LineFrame())
	val selectionKey: SelectionKey? get() = keyLoop.channels.firstOrNull()
	val worker = TcpWorker(workCallback)
	var backlog = 100

	init {
		keyLoop.callback = this
	}

	override fun onKeyAcceptable(key: SelectionKey) {
		val svr = key.channel() as ServerSocketChannel
		val client = svr.accept()//ex
		if (worker.isOpen) {
			worker.add(client)
		} else {
			client.close()
		}
	}


	val isOpen: Boolean
		get() = this.keyLoop.isOpen

	@Synchronized
	fun start(port: Int) {
		if (isOpen) {
			throw IllegalStateException("已存在是start状态")
		}
		val ch = ServerSocketChannel.open()
		ch.configureBlocking(false)
		try {
			ch.socket().bind(InetSocketAddress(port), backlog)
		} catch (ex: Exception) {
			ex.printStackTrace()
			ch.close()
			throw ex
		}
		worker.start()
		keyLoop.startLoop()
		keyLoop.add(ch, SelectionKey.OP_ACCEPT)
	}

	@Synchronized
	fun stop() {
		keyLoop.stopLoop()
		worker.stop()
	}

}
