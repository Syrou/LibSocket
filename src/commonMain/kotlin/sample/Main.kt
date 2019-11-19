package sample

fun main(args: Array<String>) {
    println("IS THIS IP: ${SokketUtil.isValidIP4Adress("127.0.0.1")}")
    /*test()
    val socket = LibSokketClient()
    socket.connect("localhost", 24)
    socket.send("Hiii this is a text")
    socket.send(byteArrayOf(0x45, 0x45, 0x45))
    socket.recv()
    socket.disconnect()
    //while(1==1){}*/
    val libSokketServer = LibSokketServer(9099, SocketType.TCP)
    libSokketServer.start()
}

