package sample

import java.net.Socket
import java.io.DataOutputStream
import java.lang.Exception


actual class LibSocket actual constructor():Base() {
    var socket:Socket? = null
    var mAddress:String? = null
    var mPort:Int = 0
    actual fun connect(address:String, port:Int) {
        mAddress=address
        mPort=port
        try {
            socket = Socket(address, port)
            socket?.let {
                connected = true;
            }
        }catch (e:Exception){
            connected = false
            println("LibSocket Error -> ${e.localizedMessage}")
        }
    }

    actual fun disconnect() {
        socket?.close()
        socket?.let {
            connected=false
        }
    }

    actual fun recv() {
        while(connected) {
            val checkSocketedConnected = mAddress?.let { Util.isSocketAliveUitlitybyCrunchify(it, mPort) } ?: false
            if(!checkSocketedConnected){
                return
            }
            socket?.let {
                try {
                    val inputStream = it.getInputStream()
                    val length = inputStream.available()
                    if(length <= 0){
                        return@let
                    }
                    val data = inputStream.readNBytes(length)
                    if(data.isNotEmpty()) {
                        val byteArray: ByteArray = ByteArray(data.size) { pos -> data[pos] }
                        println(String(byteArray))
                    }
                }catch (e:Exception){
                    println("LibSocket Recv Error -> ${e.localizedMessage}")
                    connected=false
                }
            }
        }
    }

    actual fun send(text: String) {
            send(text.toByteArray())
    }

    actual fun send(byteArray: ByteArray) {
        if(connected) {
            val outToServer = DataOutputStream(socket?.getOutputStream())
            outToServer.write(byteArray, 0, byteArray.size)
        }
    }
}