import kotlinx.coroutines.*

fun main() = runBlocking {
    launch { doWorld() }
    println("hello")
}

suspend fun doWorld() {
    delay(1000L)
    println("world")
}