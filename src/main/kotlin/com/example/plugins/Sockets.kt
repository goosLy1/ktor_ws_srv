package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val connections = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

    routing {
        webSocket("/ws") { // websocketSession
            val clientId = UUID.randomUUID().toString()
            connections[clientId] = this
            println("Client $clientId connected")

            try {
                send("Your client ID: $clientId")

                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        println("Received from $clientId: $text")

                        // Пример обработки команд
                        val parts = text.split(":", limit = 2)
                        if (parts.size == 2) {
                            val targetId = parts[0] // Идентификатор целевого устройства
                            val message = parts[1] // Сообщение для целевого устройства
                            connections[targetId]?.send(Frame.Text("$clientId: $message"))
                        } else {
                            send("Invalid message format. Use: <targetClientId>:<message>")
                        }
                    }
                }
            } finally {
                connections.remove(clientId)
                println("Client $clientId disconnected")
            }
        }
    }
}
