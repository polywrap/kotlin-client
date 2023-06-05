package io.polywrap.plugins.http

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.polywrap.core.Invoker
import io.polywrap.core.msgpack.toMsgPackMap
import io.polywrap.plugin.PluginFactory
import io.polywrap.plugin.PluginPackage
import io.polywrap.plugins.http.wrap.*
import io.ktor.client.statement.HttpResponse as KtorHttpResponse

// TODO: I would like to re-use the same HttpClient instance for all requests,
//  but I need to somehow close it when the plugin is unloaded.

/**
 * A plugin for making HTTP requests.
 *
 * @property config An optional configuration object for the plugin.
 */
class HttpPlugin(config: Config? = null) : Module<HttpPlugin.Config?>(config) {

    /**
     * Configuration class for HttpPlugin.
     *
     * @property httpClient An optional custom Ktor HttpClient instance to be used by the plugin.
     * If not provided, a new instance will be created for each request.
     * The plugin will not automatically close the custom client if it is provided.
     */
    class Config(val httpClient: HttpClient? = null)

    override suspend fun get(args: ArgsGet, invoker: Invoker): Response? {
        return request(HttpMethod.Get, args.url, args.request)
    }

    override suspend fun post(args: ArgsPost, invoker: Invoker): Response? {
        return request(HttpMethod.Post, args.url, args.request)
    }

    private suspend fun request(httpMethod: HttpMethod, url: String, request: Request?): Response {
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
                    request?.urlParams?.map?.forEach { (key, value) ->
                        parameters.append(key, value)
                    }
                }
                headers {
                    if (request?.responseType == ResponseType.TEXT) {
                        append(HttpHeaders.Accept, "text/*")
                    }
                    request?.headers?.map?.forEach { (key, value) ->
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

        val responseBody: String = if (request?.responseType == ResponseType.BINARY) {
            response.readBytes().encodeBase64()
        } else {
            response.bodyAsText()
        }

        val responseHeaders = response.headers.toMap().mapValues {
            if (it.value.size == 1) it.value.first() else it.value.toString()
        }

        return Response(
            status = response.status.value,
            statusText = response.status.description,
            headers = responseHeaders.toMsgPackMap(),
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
