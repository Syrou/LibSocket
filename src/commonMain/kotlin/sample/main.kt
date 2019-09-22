package sample

fun main() {
    val socket = LibSocket()
    socket.connect("192.168.1.184", 24)
    socket.send("Hiii this is a text")
    socket.send(byteArrayOf(0x45, 0x45, 0x45))
    socket.disconnect()
    //while(1==1){}
}