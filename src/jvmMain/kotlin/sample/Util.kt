package sample

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.Socket
import java.net.InetSocketAddress
import java.net.SocketAddress

object Util {
    fun isSocketAliveUitlitybyCrunchify(hostName: String, port: Int): Boolean {
        var isAlive = false

        // Creates a socket address from a hostname and a port number
        val socketAddress = InetSocketAddress(hostName, port)
        val socket = Socket()

        // Timeout required - it's in milliseconds
        val timeout = 1000

        isAlive = try {
            socket.connect(socketAddress, timeout)
            socket.close()
            true

        } catch (exception: SocketTimeoutException) {
            false
        } catch (exception: IOException) {
            false
        }

        return isAlive
    }
}