package com.example.plugins

import com.example.WebsocketMessage
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 30.seconds
        timeout = 60.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
    }

    val connections = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

    routing {
        webSocket("/ws") { // websocketSession
//            val clientId = UUID.randomUUID().toString()

            val clientId = call.request.headers["Client-Id"]
            if (clientId.isNullOrEmpty()) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing clientId"))
                return@webSocket
            }

            connections[clientId] = this
            println("Client $clientId connected")


            try {
//                send("Your client ID: $clientId")


                launch {
                    while (isActive) {
                        delay(20.seconds)
//                        send(Frame.Text("ping"))
                        try {
                            sendSerialized(WebsocketMessage(clientId, "ping"))
                        } catch (e: Exception) {
                            println("Failed to send ping to $clientId: ${e.message}")
                            break
                        }
                    }
                }

//                incoming.consumeEach { frame ->
//                    try {
//                        if (frame is Frame.Text) {
//                            val message = receiveDeserialized<WebsocketMessage>()
//                            println("Received from $clientId: $message")
//
//                            connections[message.clientId]?.sendSerialized(
//                                WebsocketMessage(
//                                    clientId,
//                                    message.command,
//                                    message.data
//                                )
//                            )
//                        } else {
//                            throw SerializationException("Invalid frame type: expected Frame.Text")
//                        }
//                    } catch (e: SerializationException) {
//                        sendSerialized(WebsocketMessage(clientId, "error", "invalid json"))
//                    } catch (e: Exception) {
//                        println("Error processing message from $clientId: ${e.message}")
//                        throw e
//                    }
//                }


                while (true) {
                    val message = receiveDeserialized<WebsocketMessage>()
                    println("Received from $clientId: $message")

                    connections[message.clientId]?.sendSerialized(
                        WebsocketMessage(
                            clientId,
                            message.command,
                            message.data
                        )
                    )
                }

//                incoming.consumeEach { frame ->
//                    if (frame is Frame.Text) {
//                        val text = frame.readText()
//                        println("Received from $clientId: $text")
//
//                        // Пример обработки команд
//                        val parts = text.split(":", limit = 2)
//                        if (parts.size == 2) {
//                            val targetId = parts[0] // Идентификатор целевого устройства
//                            val message = parts[1] // Сообщение для целевого устройства
//                            connections[targetId]?.send(Frame.Text("$clientId: $message"))
//                        } else {
//                            send("Invalid message format. Use: <targetClientId>:<message>")
//                        }
//                    }
//                }
            } catch (e: SerializationException) {
                sendSerialized(WebsocketMessage(clientId, "error", "invalid json"))
            } catch (e: ClosedReceiveChannelException) {
                println("Client $clientId disconnected normally")
            } catch (e: Exception) {
                println("Client $clientId disconnected with error: ${e.message}")
            } finally {
                connections.remove(clientId)
                println("Client $clientId disconnected")
            }
        }
    }
}
