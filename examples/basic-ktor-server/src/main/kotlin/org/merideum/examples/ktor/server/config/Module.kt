package org.merideum.examples.ktor.server.config

import io.ktor.client.HttpClient
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.merideum.core.api.SimpleScriptExecutor
import org.merideum.examples.ktor.server.ExternalResource
import org.merideum.examples.ktor.server.ExternalResourceResolver

fun Application.module() {

    install(ContentNegotiation) {
        jackson()
    }

    val httpClient = prepareHttpClient()

    val resourceResolver = ExternalResourceResolver(mapOf("org.requestscript.example.HelloWorld" to ExternalResource("http://localhost:8081/rqs/resource/org/requestscript/example/HelloWorld", httpClient)))

    routing {
        route("/merideum") {

            post {
                val requestRaw = this.call.receiveText()

                val executionResult = SimpleScriptExecutor(resourceResolver).execute(requestRaw)
                call.respond(executionResult.toResponse())
            }
        }
    }
}

fun prepareHttpClient() = HttpClient {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        jackson()
    }
}
