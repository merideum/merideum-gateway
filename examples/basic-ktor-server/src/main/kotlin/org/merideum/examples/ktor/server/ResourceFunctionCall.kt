package org.merideum.examples.ktor.server

data class ResourceFunctionCall(
    val function: FunctionCall,
)

data class FunctionCall(
    val name: String,
    val params: Map<String, Any?>,
)
