package sample

import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.select

data class ClientSokket(var socket:SOCKET, val clientAddress: sockaddr )

actual class LibSokketServer actual constructor(val port:Int, val type:SocketType) {

    var clientSocket:SOCKET = INVALID_SOCKET
    var listenSocket:SOCKET = INVALID_SOCKET
    val wsaData2:LPWSADATA? = null
    var wsaData:WSADATA? = null
    var sendResult:Int = 0
    var clientArray:ArrayList<ClientSokket> = arrayListOf<ClientSokket>()
    var result:Int = 0;

    var readSet:fd_set? = null
    var writeSet:fd_set? = null
    var exceptSet:fd_set? = null

    @ExperimentalUnsignedTypes
    actual fun start():Int{

        memScoped {
            wsaData = alloc<WSADATA>()
            result = WSAStartup(MAKEWORD(2,2), wsaData?.ptr)
            var dataToRead:ByteArray = byteArrayOf()
            if (result != 0) {
                printf("WSAStartup failed with error: %d\n", result);
                return 1;
            }

            val serverAddr = alloc<sockaddr_in>()
            val socketType = when(type){
                SocketType.TCP -> IPPROTO_TCP
                SocketType.UDP -> IPPROTO_UDP
            }

            listenSocket = socket(AF_INET, SOCK_STREAM, socketType)
            if (listenSocket == INVALID_SOCKET) {
                printf("socket failed with error: %ld\n", WSAGetLastError());
                WSACleanup();
                return 1;
            }

            with(serverAddr) {
                memset(this.ptr, 0, sockaddr_in.size.convert())
                sin_family = AF_INET.convert()
                sin_addr.S_un.S_addr = INADDR_ANY
                sin_port = htons(port.toUShort())
            }



            if(bind(listenSocket, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert()) == SOCKET_ERROR){
                printf("Bind failed with error code : %d" , WSAGetLastError())
                closesocket(listenSocket)
                WSACleanup()
                return 1
            }

            println("Binding done.")



            result = listen(listenSocket, SOMAXCONN);
            if (result == SOCKET_ERROR) {
                printf("listen failed with error: %d\n", WSAGetLastError());
                closesocket(listenSocket);
                WSACleanup();
                return 1;
            }

            /*clientSocket = accept(listenSocket, null, null)
            if(clientSocket == INVALID_SOCKET){
                printf("accept failed with error: %d\n", WSAGetLastError())
                closesocket(listenSocket)
                WSACleanup()
                return 1;
            }*/
            //set_nonblock(clientSocket)
            while({result = getReadStatus(listenSocket); result}() != SOCKET_ERROR) {

                if(result == 1){
                    println("New client connected!")

                    val clientAddress = alloc<sockaddr>()
                    val socklen = alloc<platform.windows.socklen_tVar>()
                    socklen.value = sockaddr.size.convert()
                    clientSocket = accept(listenSocket, clientAddress.ptr, socklen.ptr)
                    if(clientSocket == INVALID_SOCKET){
                        println("accept failed with error: ${WSAGetLastError()}");
                        return 1
                    }
                    set_nonblock(clientSocket)
                    val clientSockket = ClientSokket(clientSocket, clientAddress)
                    clientArray.add(clientSockket)
                    println("Currently connected clients: ${clientArray.size}")
                }else{
                    if(result != 0)
                    println("Current result: $result")

                    clientArray.forEach lit@{iterated ->
                        val status = getReadStatus(iterated.socket)
                        if(status == 1){
                            if(iterated.socket == INVALID_SOCKET){
                                println("Invalid socket returning..")
                                return@lit
                            }
                            val dataAvailableToRead = getAvailableBytes(iterated.socket)
                            if(dataAvailableToRead > 0) {
                                val used = dataToRead.size
                                dataToRead = dataToRead.copyOf(dataAvailableToRead + used)
                                println("Do we have any data to read?: $dataAvailableToRead")
                                result = recv(iterated.socket, dataToRead.refTo(used), dataAvailableToRead, 0)

                                if (result == SOCKET_ERROR) {
                                    println("Client $iterated recv() error ${WSAGetLastError()}")
                                    return 1
                                } else {
                                    println("Client sent: ${dataToRead.decodeToString()}")
                                }
                            }else{
                                iterated.socket = INVALID_SOCKET
                                println("User most likly disconnected.")
                                println("Removing user")
                                //println("No data to read...")
                            }
                        }else if(status == SOCKET_ERROR){
                            println("\r client $iterated read error")
                            val result:Int = endBrokerSocket(iterated.socket)
                            if(result == EXIT_FAILURE){
                                return EXIT_FAILURE
                            }
                            iterated.socket = INVALID_SOCKET
                        }
                    }
                    if(dataToRead.isNotEmpty()){
                        println("Sending: ")
                        println(dataToRead.decodeToString())
                        println("To all clients")

                        clientArray.forEach lit@{ iterated ->
                            if(iterated.socket == INVALID_SOCKET){
                                return@lit
                            }

                            println("Im about to send!")
                            result = send(iterated.socket, dataToRead.refTo(0), dataToRead.size, 0)
                            if(result == SOCKET_ERROR){
                                println("client $iterated send error ${WSAGetLastError()}")
                                val result = endBrokerSocket(iterated.socket)
                                if(result == EXIT_FAILURE){
                                    return EXIT_FAILURE
                                }
                                iterated.socket = INVALID_SOCKET
                            }
                        }
                        println("Should have sent to all clients now... clearing")
                        dataToRead = byteArrayOf()
                    }
                    if(clientArray.size > 0){
                        clientArray.forEach {
                            if(it.socket == INVALID_SOCKET){
                                clientArray.remove(it)
                            }
                        }
                    }
                }
            }

            clientArray.forEach {
                val result = endBrokerSocket(it.socket)
                if(result == EXIT_FAILURE){
                    return EXIT_FAILURE
                }
                it.socket = INVALID_SOCKET
            }
            return EXIT_SUCCESS
        }
    }

    private fun endBrokerSocket(socket: SOCKET): Int {
        var result = shutdown(socket, SD_BOTH)
        if(result != 0){
            println("socket shutdown() error ${WSAGetLastError()}")
        }
        result = closesocket(socket)
        if(result != 0){
            println("socket closesocket() error ${WSAGetLastError()}")
            return EXIT_FAILURE
        }
        WSACleanup()
        return EXIT_SUCCESS
    }


    fun set_nonblock(socket: SOCKET) {
        val nNoBlock = uintArrayOf(1u)
        if(ioctlsocket(socket, FIONBIO.toInt(),nNoBlock.refTo(0)) == SOCKET_ERROR){
            println("shutdown failed with error: ${WSAGetLastError()}")
            closesocket(socket);
            WSACleanup()
        }
    }

    fun FD_ZERO(fdSet: fd_set){
        fdSet.fd_count = 0u
        fdSet.fd_array[0] = 0u
    }

    fun FD_SET(socket: SOCKET, fdSet: fd_set):fd_set{
        fdSet.fd_array[0] = socket
        fdSet.fd_count = 1u
        return fdSet
    }

    fun getReadStatus(socket:SOCKET):Int {
        memScoped {

            val max_sd:SOCKET = socket
            val timeval = cValue<timeval>{
                this.tv_sec = 0
                this.tv_usec = 0
            }

            val fdSet = cValue<fd_set> {
                this.fd_array[0] = socket
                this.fd_count = 1u
            }

            var result = select((max_sd + 1u).toInt(), fdSet, null, null, timeval.ptr)
            //println("Select status: $result")
            if(result == SOCKET_ERROR){
                result = WSAGetLastError()
            }else if(result != 0 && result != 1){
                println("select() error $result")
                return SOCKET_ERROR
            }
            return result
        }
    }

    fun htons(value: UShort) = ((value.toInt() ushr 8) or (value.toInt() shl 8)).toUShort()

    fun MAKEWORD(x:Int, y:Int):UShort{
        val stuff = y shl 8 or x
        return stuff.toUShort()
    }

    val availableBytes
        get() = run {
            val bytes_available = uintArrayOf(0u, 0u)
            //platform.windows.ioctlsocket(sockfd, platform.windows.FIONREAD, bytes_available.refTo(0).reinterpret())
            platform.windows.ioctlsocket(clientSocket, platform.windows.FIONREAD, bytes_available.refTo(0))
            checkErrors("ioctlsocket")
            bytes_available[0].toInt()
        }

    fun getAvailableBytes(clientSocket: SOCKET):Int{
        val data = uintArrayOf(0u, 0u)
        platform.windows.ioctlsocket(clientSocket, platform.windows.FIONREAD, data.refTo(0))
        return data[0].toInt()
    }

    fun checkErrors(name: String = "") {
        val error = platform.windows.WSAGetLastError()
        if (error != 0) {
            error("WSA error($name): $error")
        }
    }
}

fun throwUnixError(): Nothing {
    perror(null) // TODO: store error message to exception instead.
    throw Error("UNIX call failed")
}

inline fun Int.ensureUnixCallResult(predicate: (Int) -> Boolean): Int {
    if (!predicate(this)) {
        throwUnixError()
    }
    return this
}