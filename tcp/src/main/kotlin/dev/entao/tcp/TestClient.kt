package dev.entao.tcp

import java.nio.channels.SelectionKey


fun main() {

	val callback = object : TcpClientCallback {
		override fun onClientReadFrame(key: SelectionKey, data: ByteArray) {
			println(data.strUTF8)
		}

		override fun onClientConnected(key: SelectionKey) {
			println("Connected")
			key.writeLine("Yang")
		}

		override fun onClientConnectFailed(key: SelectionKey) {
			println("Connect Failed")
		}

		override fun onClientFinish() {
			println("Finished")
		}
	}

	val a = TcpClient(callback)
	a.start("localhost", 9000)
	println("client Started")
	Thread.sleep(30_000)
	a.stop()
	println("client END")
}