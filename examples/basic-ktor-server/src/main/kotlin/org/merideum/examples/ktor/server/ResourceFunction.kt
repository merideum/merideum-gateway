package org.merideum.examples.ktor.server

class ResourceFunction(
    val name: String,
    val params: List<FunctionParam>,
    val returns: FunctionParamType,
)

class FunctionParam(
    val name: String,
    val type: FunctionParamType,
)
