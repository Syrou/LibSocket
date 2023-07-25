package eu.syrou.sokket

import kotlinx.cinterop.*
import kotlinx.cinterop.internal.CCall
import platform.posix.fd_set
import platform.posix.size_t
import platform.posix.timeval

actual typealias addrinfo = platform.posix.addrinfo
actual fun socket(addrInfo: CPointer<addrinfo>?): Int {
    if(addrInfo == null){
        return -1
    }
    return platform.posix.socket(addrInfo.pointed.ai_family, addrInfo.pointed.ai_socktype, addrInfo.pointed.ai_protocol)
}

actual fun close(socket: Int): Int{
    return platform.posix.close(socket)
}

actual fun setsockopt(socket: Int, level: Int, optname: Int, optval: CPointer<ByteVar>, optlen: Int): Int {
    return platform.posix.setsockopt(socket, level, optname, optval, optlen.toUInt())
}

actual fun select(
    nfds: Int,
    readfds: CValuesRef<fd_set>?,
    writefds: CValuesRef<fd_set>?,
    exceptfds: CValuesRef<fd_set>?,
    timeout: Long
): Int = memScoped {
    val innerTimeout = alloc<timeval>()
    innerTimeout.tv_sec = (timeout / 1000)
    innerTimeout.tv_usec = (timeout % 1000 * 1000)
    return platform.posix.select(nfds, readfds, writefds, exceptfds, innerTimeout.ptr)
}

actual fun freeaddrinfo(ai: CPointer<addrinfo>){
    return platform.posix.freeaddrinfo(ai)
}

actual fun connect(socket: Int, addressInfo: CPointer<addrinfo>?):Int {
    if(addressInfo?.pointed == null){
        return -1
    }
    return platform.posix.connect(socket, addressInfo.pointed.ai_addr, addressInfo.pointed.ai_addrlen)
}

actual fun send(socket: Int, data: ByteArray):Int{
    val dataAsCValues = data.toCValues()
    return platform.posix.send(socket, dataAsCValues, dataAsCValues.size.toULong(), 0).toInt()
}

actual fun recv(fd: Int, buf: CValuesRef<ByteVar>?, len: size_t, flags: Int): Int = memScoped {
    val timeout = alloc<timeval>()
    timeout.tv_sec = (5000 / 1000)
    timeout.tv_usec = (5000 % 1000 * 1000)
    return platform.posix.recv(fd, buf, len, 0).toInt()
}
actual fun getaddrinfo(nodeName: String, servname: String, addressInfo: AdressInfo): CPointer<addrinfo>? = memScoped{
    val hints = alloc<addrinfo>()
    hints.ai_family = addressInfo.aiFamily
    hints.ai_socktype = addressInfo.aiSocketType
    hints.ai_protocol = addressInfo.aiProtocol
    val result: CPointerVar<addrinfo> = alloc()
    if(platform.posix.getaddrinfo(nodeName, servname, hints.ptr, result.ptr) != 0){
        return null
    }
    return result.value
}