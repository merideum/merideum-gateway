package org.merideum.examples.ktor.server.config

import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.merideum.examples.ktor.server.ExternalResource
import org.merideum.examples.ktor.server.ExternalResourceResolver
import org.merideum.examples.ktor.server.PutResource
import org.merideum.examples.ktor.server.ResourceFunctionCall
import org.requestscript.core.api.SimpleScriptExecutor

fun Application.module() {

    install(ContentNegotiation) {
        jackson()
    }

    val httpClient = prepareHttpClient()

    val resourceResolver = ExternalResourceResolver(
        mutableMapOf(
//            "org.requestscript.example.HelloWorld" to ExternalResource(
//            "http://localhost:8081",
//            "/org/requestscript/example",
//            mapOf("hello" to ResourceFunction(mapOf("name" to FunctionParamType.STRING), FunctionParamType.STRING)),
//            httpClient)
        )
    )

    routing {
        route("/rqs") {

            post {
                val requestRaw = this.call.receiveText()

                val executionResult = SimpleScriptExecutor(resourceResolver).execute(requestRaw)
                call.respond(executionResult.toResponse())
            }

            route("resource") {

                route("/{path...}") {
                    get {
                        val resourcePath = call.parameters.getAll("path")!!.joinToString(separator = ".") { it }

                        val resources = resourceResolver.resources.filter {
                            it.key.contains(resourcePath)
                        }.map {
                            it.value
                        }

                        call.respond(mapOf("resources" to resources))
                    }

                    post {
                        val resourcePath = call.parameters.getAll("path")!!.joinToString(separator = ".") { it }

                        val body = call.receive<ResourceFunctionCall>()

                        val result = (resourceResolver
                            .get(resourcePath) ?: return@post call.response.status(HttpStatusCode.NotFound))
                            .callFunction(body.function.name, body.function.params)

                        if (result != Unit) {
                            call.respondText(result.toString())
                        }
                    }

                    put {
                        val pathAndName = call.parameters.getAll("path")!!.joinToString(separator = "/") { it }
                        val nameStartIndex = pathAndName.lastIndexOf("/") + 1
                        val resourceName = pathAndName.substring(nameStartIndex)
                        val resourcePath = pathAndName.substring(0, nameStartIndex - 1)

                        val request = call.receive<PutResource>()

                        val resource = ExternalResource(
                            request.host,
                            resourceName,
                            resourcePath,
                            request.functions,
                            httpClient,
                        )

                        resourceResolver.add("${resourcePath.replace("/", ".")}.$resourceName", resource)

                        call.response.status(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}

fun prepareHttpClient() = HttpClient {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        jackson()
    }
}
