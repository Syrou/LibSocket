import eu.syrou.sokket.*
import kotlinx.cinterop.*
import kotlinx.datetime.Clock
import platform.posix.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>) {
    val sokket = Sokket(SokketConfiguration(
        hostname = "ams01.login.pathofexile.com",
        port = 20481,
        adressInfo = AdressInfo(
            aiFamily = AF_INET,
            aiSocketType = SOCK_STREAM,
            aiProtocol = IPPROTO_TCP
        ),
        recieveTimeout = 5.toDuration(DurationUnit.SECONDS)
    ))
    sokket.connect()

    //val sendRes = sokket.send("Hej detta är text".utf8.getBytes())
    println("START NOW: ${Clock.System.now()}")
    val response = sokket.recv(1024)
    println("RECV DONE NOW: ${Clock.System.now()}")
    println("resp: ${response.toHex()}")
    sokket.disconnect()
}

private fun ByteArray.toHex(): String = asUByteArray().joinToString("-") { it.toString(radix = 16).padStart(2, '0') }
