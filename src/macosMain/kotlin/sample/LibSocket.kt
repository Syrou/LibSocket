package sample

import kotlinx.cinterop.*
import platform.posix.*

actual class LibSocket actual constructor() : Base() {
    var sock = 0
    actual fun connect(address:String, port:Int) {
        memScoped {
            sock = socket(AF_INET, SOCK_STREAM, 0)
            if(sock.toInt() < 0){
                perror("socket failed: ${sock.toInt()}\n")
                exit(EXIT_FAILURE)
            }else{
                println("Socket created!")
            }
            val sockAddress = alloc<sockaddr_in>()
            with(sockAddress){
                memset(this.ptr, 0, sockaddr_in.size.convert())
                sin_family = AF_INET.convert()
                sin_port = htons(port.toUShort())
                if(inet_pton(AF_INET, address, sin_addr.ptr) <= 0){
                    println("Invalid address/ Address not supported");
                }else{
                    println("A valid ip-address was entered")
                }
            }
            val result = connect(sock, sockAddress.ptr.reinterpret(), sockaddr_in.size.convert())
            if(result < 0){
                platform.posix.close(sock)
            }else{
                println("We connected!")
                connected=true
            }
        }
    }

    actual fun disconnect() {
        platform.posix.close(sock)
    }

    actual fun send(text:String) {
        if(connected) {
            val byteArray =  text.encodeToByteArray()
            platform.posix.send(sock, byteArray.refTo(0), byteArray.size.convert(), 0)
            println("We should have sent something?")
        }else{
            println("We can not send when we are not connected")
        }
    }

    actual fun recv() {
    }

    actual fun send(byteArray: ByteArray) {
        if(connected){
            println("We should have sent something?")
            platform.posix.send(sock, byteArray.refTo(0), byteArray.size.convert(), 0)
        }else{
            println("We can not send when we are not connected")
        }
    }
}