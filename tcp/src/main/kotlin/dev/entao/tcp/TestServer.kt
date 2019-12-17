package dev.entao.tcp

import java.nio.channels.SelectionKey


fun main() {
	val callback = object : TcpCallback {
		override fun onReadFrame(key: SelectionKey, data: ByteArray) {
			println(data.strUTF8)
			key.writeLine("Hello " + data.strUTF8)
		}

		override fun onKeyAdded(key: SelectionKey) {
			println("onAdded")
		}

		override fun onKeyRemoved(key: SelectionKey) {
			println("onRemoved")
		}
	}
	val a = TcpServer(callback)
	a.start(9000)
	println("Main Started")
	Thread.sleep(20_000)
	a.stop()
	println("Main END")
}