package org.merideum.examples.ktor.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.merideum.core.interpreter.Resource
import org.merideum.core.interpreter.ResourceResolver

class ExternalResourceResolver(val resources: Map<String, ExternalResource>): ResourceResolver {
    override fun get(name: String): Resource? = resources[name]
}

class ExternalResource(
    // The url path including the path of the resource (and its name)
    val path: String,
    val httpClient: HttpClient,
): Resource {
    override fun callFunction(functionName: String, params: Map<String, Any?>): Any? {
        val result: String = runBlocking {
            httpClient.post(path) {
                contentType(ContentType.Application.Json)
                setBody(ResourceFunctionCall(FunctionCall(functionName, params)))
            }.body()
        }

        return result
    }
}
