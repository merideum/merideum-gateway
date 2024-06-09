package org.merideum.examples.ktor.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.requestscript.core.interpreter.Resource
import org.requestscript.core.interpreter.ResourceResolver

class ExternalResourceResolver(val resources: MutableMap<String, ExternalResource>): ResourceResolver {
    override fun get(name: String): Resource? = resources[name]

    fun add(fullResourcePath: String, resource: ExternalResource) {
        resources[fullResourcePath] = resource
    }
}

class ExternalResource(
    // The url path including
    private val host: String,
    val name: String,
    // the path of the resource (and its name)
    val path: String,
    val functions: List<ResourceFunction>,
    private val httpClient: HttpClient,
): Resource {
    override fun callFunction(functionName: String, params: Map<String, Any?>): Any? {
        val result = runBlocking {
            httpClient.post("$host/rqs/resource/$path/$name") {
                contentType(ContentType.Application.Json)
                setBody(ResourceFunctionCall(FunctionCall(functionName, params)))
            }.body<ExternalCallResult>()
        }

        return result.returnValue
    }
}

class ExternalCallResult(
    val returnValue: Any?
)
