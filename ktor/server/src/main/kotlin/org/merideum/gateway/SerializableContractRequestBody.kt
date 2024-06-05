package org.merideum.gateway

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SerializableContractRequestBody(
    val parameters: JsonObject
)
