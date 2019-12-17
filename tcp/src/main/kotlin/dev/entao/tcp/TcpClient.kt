@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.tcp

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

interface TcpClientCallback {
	fun onClientConnected(key: SelectionKey) {}
	fun onClientConnectFailed(key: SelectionKey) {}
	fun onClientReadIdle(key: SelectionKey) {}
	fun onClientReadFrame(key: SelectionKey, data: ByteArray) {}
	fun onClientFinish() {}
}

class TcpClient(val callback: TcpClientCallback) : TcpCallback {
	private val serviceLoop = TcpLoop(LineFrame())
	val selectionKey: SelectionKey? get() = serviceLoop.channels.firstOrNull()

	init {
		serviceLoop.callback = this
	}

	override fun onKeyConnectFailed(key: SelectionKey) {
		callback.onClientConnectFailed(key)
	}

	override fun onKeyConnected(key: SelectionKey) {
		callback.onClientConnected(key)
	}

	override fun onKeyReadIdle(key: SelectionKey) {
		callback.onClientReadIdle(key)
	}

	override fun onReadFrame(key: SelectionKey, data: ByteArray) {
		callback.onClientReadFrame(key, data)
	}

	override fun onLoopFinish() {
		callback.onClientFinish()
	}

	override fun onKeyRemoved(key: SelectionKey) {
		val sel = key.selector()
		if (sel.isOpen) {
			sel.close()
		}
	}

	val isOpen: Boolean
		get() = this.serviceLoop.isOpen

	@Synchronized
	fun start(host: String, port: Int) {
		if (isOpen) {
			throw IllegalStateException("已存在是start状态")
		}
		serviceLoop.startLoop()

		val inet = InetSocketAddress(host, port)
		val ch = SocketChannel.open()
		ch.configureBlocking(false)
		ch.connect(inet)
		serviceLoop.add(ch, SelectionKey.OP_CONNECT)
	}

	@Synchronized
	fun stop() {
		serviceLoop.stopLoop()
	}

	val isActive: Boolean
		get() {
			val key = this.selectionKey ?: return false
			if (!isOpen || !key.isValid) {
				return false
			}
			val ch = key.channel() as SocketChannel
			return ch.isOpen && ch.isConnected
		}
}