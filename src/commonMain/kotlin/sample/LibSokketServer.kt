package sample

expect class LibSokketServer(port:Int, type:SocketType) {
  fun start():Int
}