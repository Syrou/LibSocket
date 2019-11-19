package sample

expect class LibSokketClient() : Base {
    fun connect(address: String, port: Int)
    fun disconnect()
    fun send(text:String)
    fun send(byteArray: ByteArray)
    fun recv()
}