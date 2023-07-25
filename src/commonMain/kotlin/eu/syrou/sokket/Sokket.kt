package eu.syrou.sokket

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

expect class addrinfo : CStructVar
expect fun socket(addrInfo: CPointer<addrinfo>?): Int
expect fun close(socket: Int): Int
expect fun freeaddrinfo(ai: CPointer<addrinfo>)
expect fun connect(socket: Int, addressInfo: CPointer<addrinfo>?):Int
expect fun send(socket: Int, data: ByteArray): Int

expect fun setsockopt(socket: Int, level: Int, optname: Int, optval: CPointer<ByteVar>, optlen: Int): Int

expect fun select(
    nfds: Int,
    readfds: CValuesRef<fd_set>?,
    writefds: CValuesRef<fd_set>?,
    exceptfds: CValuesRef<fd_set>?,
    timeout: Long
): Int
expect fun recv(fd: Int, buf: CValuesRef<ByteVar>?, len: size_t, flags: Int):Int
expect fun getaddrinfo(nodeName: String, servname: String, addressInfo: AdressInfo): CPointer<addrinfo>?

data class AdressInfo(
    val aiFamily: Int,
    val aiSocketType: Int,
    val aiProtocol: Int
)

data class SokketConfiguration(
    val hostname: String = "127.0.0.1",
    val port: Int = 8080,
    val adressInfo: AdressInfo = AdressInfo(
        aiFamily = AF_UNSPEC,
        aiSocketType = SOCK_STREAM,
        aiProtocol = IPPROTO_TCP
    ),
    val recieveTimeout: Duration = 5.toDuration(DurationUnit.SECONDS),
    val sendTimeout: Duration = 5.toDuration(DurationUnit.SECONDS),
)
class Sokket(val configuration: SokketConfiguration) {
    var sock:Int = -1
    fun connect(): Boolean {
        init_sockets()
        val addrinfo = getaddrinfo(configuration.hostname, configuration.port.toString(), configuration.adressInfo)
        if (addrinfo == null) {
            println("getaddrinfo failed with: $addrinfo")
            return false
        }
        sock = socket(addrinfo)
        if(sock == -1){
            println("Socket creation failed")
            return false
        }

        val connectRes = connect(sock, addrinfo)
        if(connectRes == -1){
            println("connect failed")
            disconnect()
        }
        freeaddrinfo(addrinfo)
        return true
    }

    fun setsockopt(optname: Int, optval: CPointer<ByteVar>, optlen: Int ): Int{
        return setsockopt(sock, SOL_SOCKET, optname, optval, optlen)
    }

    fun recv(numberOfBytes:Int):ByteArray = memScoped{
        val readSet = alloc<fd_set>()
        posix_FD_ZERO(readSet.ptr)
        posix_FD_SET(sock.convert(), readSet.ptr)

        val ready = select(
            sock + 1,
            readSet.ptr,
            null,
            null,
            configuration.recieveTimeout.inWholeMilliseconds
        )
        if (ready == -1) {
            return byteArrayOf()
        }

        if (posix_FD_ISSET(sock, readSet.ptr) != 0) {

            val buffer = ByteArray(numberOfBytes)
            val bytesRead = recv(sock, buffer.refTo(0), numberOfBytes.toULong(), 0)
            if (bytesRead == -1 || bytesRead == 0) {
                return byteArrayOf()
            }
            return buffer.copyOf(bytesRead)
        }
        return byteArrayOf()
    }

    fun send(byteArray: ByteArray): Int = memScoped{
        if(sock == -1){
            return -1
        }
        val writeSet = alloc<fd_set>()
        posix_FD_ZERO(writeSet.ptr)
        posix_FD_SET(sock.convert(), writeSet.ptr)

        val ready = select(sock + 1, null, writeSet.ptr, null, configuration.sendTimeout.inWholeMilliseconds)
        if (ready == -1) {
            println("select for writing failed")
            return -1
        }

        if (posix_FD_ISSET(sock, writeSet.ptr) != 0) {
            val sentBytes = send(sock, byteArray)
            if (sentBytes == -1) {
                println("send failed")
            }
            return sentBytes
        } else {
            println("send timeout")
        }
        return -1
    }

    fun disconnect(){
        close(sock)
        deinit_sockets()
        sock = -1
    }
}