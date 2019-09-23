package sample

import java.net.Socket
import java.io.DataOutputStream

actual class LibSocket actual constructor():Base() {
    var socket:Socket? = null
    actual fun connect(address:String, port:Int) {
        socket = Socket(address, port)
    }

    actual fun disconnect() {
        socket?.close()
    }

    actual fun recv() {
    }

    actual fun send(text: String) {
        send(text.toByteArray())
    }

    actual fun send(byteArray: ByteArray) {
        val outToServer = DataOutputStream(socket?.getOutputStream())
        outToServer.write(byteArray, 0 , byteArray.size)
    }
}