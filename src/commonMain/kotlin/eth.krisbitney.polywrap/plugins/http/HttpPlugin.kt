package eth.krisbitney.polywrap.plugins.http

import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.plugin.PluginFactory
import eth.krisbitney.polywrap.plugin.PluginPackage
import eth.krisbitney.polywrap.plugins.http.wrapHardCoded.*
import eth.krisbitney.polywrap.plugins.http.wrapHardCoded.HttpRequest
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse as KtorHttpResponse
import io.ktor.http.*
import io.ktor.util.*

// TODO: I would like to re-use the same HttpClient instance for all requests,
//  but I need to somehow close it when the plugin is unloaded.

class HttpPlugin(config: Config? = null) : Module<HttpPlugin.Config?>(config) {

    class Config(val httpClient: HttpClient? = null)

    override suspend fun get(args: ArgsGet, invoker: Invoker): HttpResponse? {
        return request(HttpMethod.Get, args.url, args.request)
    }

    override suspend fun post(args: ArgsPost, invoker: Invoker): HttpResponse? {
        return request(HttpMethod.Post, args.url, args.request)
    }

    private suspend fun request(httpMethod: HttpMethod, url: String, request: HttpRequest?): HttpResponse {
        val client = if (config?.httpClient != null) {
            config.httpClient
        } else {
            HttpClient() {
                install(HttpTimeout)
                engine {
                    threadsCount = 1
                }
            }
        }

        val response: KtorHttpResponse = try {
            client.request(url) {
                method = httpMethod
                if (httpMethod == HttpMethod.Post) {
                    setBody(request?.body)
                }
                url {
                    request?.urlParams?.forEach { (key, value) ->
                        parameters.append(key, value)
                    }
                }
                headers {
                    if (request?.responseType == HttpResponseType.TEXT) {
                        append(HttpHeaders.Accept, "text/*")
                    }
                    request?.headers?.forEach { (key, value) ->
                        append(key, value)
                    }
                }
                timeout {
                    requestTimeoutMillis = request?.timeout?.toLong() ?: HttpTimeout.INFINITE_TIMEOUT_MS
                }
            }
        } finally {
            if (config?.httpClient == null) {
                client.close()
            }
        }

        val responseBody: String = if (request?.responseType == HttpResponseType.BINARY) {
            response.readBytes().encodeBase64()
        } else {
            response.bodyAsText()
        }

        return HttpResponse(
            status = response.status.value,
            statusText = response.status.description,
            headers = response.headers.toMap().mapValues { it.toString() },
            body = responseBody
        )
    }
}

val httpPlugin: PluginFactory<HttpPlugin.Config?> = { config: HttpPlugin.Config? ->
    PluginPackage(
        pluginModule = HttpPlugin(config),
        manifest = manifest
    )
}