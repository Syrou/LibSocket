package sample

expect class LibSocket() : Base {
    fun connect(address: String, port: Int)
    fun disconnect()
    fun send(text:String)
    fun send(byteArray: ByteArray)
    fun recv()
}