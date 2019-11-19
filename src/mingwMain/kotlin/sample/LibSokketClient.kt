package sample

import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*
import platform.windows.AF_INET
import platform.windows.IPPROTO_TCP
import platform.windows.SOCK_STREAM
import platform.windows.connect
import platform.windows.socket

actual class LibSokketClient actual constructor() : Base() {
    var sock: SOCKET = 0U
    actual fun connect(address:String, port:Int) {
        memScoped {
            val wsaData: WSAData = alloc<WSADATA>()
            val word = 0x202
            platform.windows.WSAStartup(word.toUShort(), wsaData.ptr.reinterpret())
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
                if(SokketUtil.isValidIP4Adress(address)) {
                    if (inet_pton(AF_INET, address, sin_addr.ptr) <= 0) {
                        println("Invalid address/ Address not supported");
                    } else {
                        println("A valid ip-address was entered")
                    }
                }else{
                    println("The given address is a hostname")
                    //val hostnameAddressPtr:CPointer<hostent>? = platform.windows.gethostbyname(address)
                    //val Ipnumber = hostnameAddressPtr?.asStableRef<LPHOSTENTVar>()
                    //val info = sockAddress[0]!!.pointed
                    //val inetaddr = info.ai_addr!!.pointed.sa_data
                    sin_addr.S_un.S_addr = fromHost(address)
                }
            }
            val result = connect(sock, sockAddress.ptr.reinterpret(), sockaddr_in.size.convert())
            if(result < 0){
                platform.windows.closesocket(sock)
                platform.windows.WSACleanup()
            }else{
                println("We connected!")
                connected=true
            }
        }
    }

    fun fromHost(host: String): UInt {
        memScoped {
            // gethostbyname unusable on windows
            val addr = allocArray<LPADDRINFOVar>(1)
            val res = platform.windows.getaddrinfo(host, null, null, addr)
            println("RES: $res")
            val error = platform.windows.WSAGetLastError()
            if (error != 0) {
                error("WSA error(getaddrinfo): $error")
            }
            val info = addr[0]?.pointed
            println("INFO: ${info?.ai_addr?.pointed?.sa_data?.toLong()}")
            val inetaddr = info?.ai_addr?.pointed?.sa_data
            val v0 = inetaddr?.get(0)?.toUByte()
            val v1 = inetaddr?.get(1)?.toUByte()
            val v2 = inetaddr?.get(2)?.toUByte()
            val v3 = inetaddr?.get(3)?.toUByte()
            val value: Int = (v0?.toInt() ?: 0 shl 0) or (v1?.toInt() ?: 0 shl 8) or (v2?.toInt() ?: 0 shl 16) or (v3?.toInt() ?: 0 shl 24)
            println("$v0.$v1.$v2.$v3")
            return value.toUInt()
        }
    }

    actual fun disconnect() {
        platform.windows.closesocket(sock)
        platform.windows.WSACleanup()
    }

    actual fun send(text:String) {
        if(connected) {
            platform.windows.send(sock, text, text.length, 0)
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