package sample

actual class LibSocket actual constructor():Base() {
    actual fun connect(address:String, port:Int) {
    }

    actual fun disconnect() {
    }

    actual fun recv() {
    }

    actual fun send(text: String) {
    }

    actual fun send(byteArray: ByteArray) {
    }
}