package sample

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    val name: String
}

expect fun test()

fun hello(): String = "Hello from ${Platform.name}"