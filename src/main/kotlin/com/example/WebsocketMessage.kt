package com.example

import kotlinx.serialization.Serializable

@Serializable
data class WebsocketMessage(
    val clientId: String,
    val command: String,
    val data: String? = null
)
