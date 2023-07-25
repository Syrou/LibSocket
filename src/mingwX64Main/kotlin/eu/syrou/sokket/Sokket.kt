package eu.syrou.sokket

import kotlinx.cinterop.*
import platform.posix.*
import platform.posix.shutdown
import platform.posix.size_t
import platform.windows.*
import platform.windows.closesocket
import platform.windows.connect
import platform.windows.setsockopt
import platform.windows.socket

actual typealias addrinfo = platform.windows.addrinfo

actual fun socket(addrInfo: CPointer<addrinfo>?): Int {
    if(addrInfo == null){
        return -1
    }

    return socket(addrInfo.pointed.ai_family, addrInfo.pointed.ai_socktype, addrInfo.pointed.ai_protocol).toInt()
}

actual fun close(socket: Int): Int {
    shutdown(socket.toULong(), SD_SEND)
    return closesocket(socket.toULong())
}

actual fun setsockopt(socket: Int, level: Int, optname: Int, optval: CPointer<ByteVar>, optlen: Int): Int {
    return setsockopt(socket.toULong(), level, optname, optval.toKString(), sizeOf<IntVar>().toInt())
}

actual fun select(
    nfds: Int,
    readfds: CValuesRef<fd_set>?,
    writefds: CValuesRef<fd_set>?,
    exceptfds: CValuesRef<fd_set>?,
    timeout: Long
): Int = memScoped {
    val innerTimeout = alloc<timeval>()
    innerTimeout.tv_sec = ((timeout / 1000).toInt())
    innerTimeout.tv_usec = ((timeout % 1000 * 1000).toInt())
    return platform.windows.select(nfds, readfds, writefds, exceptfds, innerTimeout.ptr)
}

actual fun freeaddrinfo(ai: CPointer<addrinfo>){
    return platform.windows.freeaddrinfo(ai)
}

actual fun connect(socket: Int, addressInfo: CPointer<addrinfo>?): Int {
    if (addressInfo?.pointed == null) {
        return -1
    }
    return connect(socket.toULong(), addressInfo.pointed.ai_addr, addressInfo.pointed.ai_addrlen.toInt())
}

actual fun send(socket: Int, data: ByteArray): Int {
    val dataAsCValues = data.toCValues()
    return send(socket.toULong(), dataAsCValues, dataAsCValues.size, 0)
}

actual fun recv(fd: Int, buf: CValuesRef<ByteVar>?, len: size_t, flags: Int): Int = memScoped {
    return platform.windows.recv(fd.toULong(), buf, len.toInt(), 0)
}

actual fun getaddrinfo(nodeName: String, servname: String, addressInfo: AdressInfo): CPointer<addrinfo>? = memScoped {
    val hints = alloc<addrinfo>()
    hints.ai_family = addressInfo.aiFamily
    hints.ai_socktype = addressInfo.aiSocketType
    hints.ai_protocol = addressInfo.aiProtocol
    val result: CPointerVar<addrinfo> = alloc()
    if(getaddrinfo(nodeName, servname, hints.ptr, result.ptr) != 0){
        println("IT does not fail here?")
        return null
    }
    return result.value
}