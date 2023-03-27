package eth.krisbitney.polywrap.plugins.http

import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.plugins.http.wrapHardCoded.*
import eth.krisbitney.polywrap.plugins.http.wrapHardCoded.HttpRequest
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse as KtorHttpResponse
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.core.*

// TODO: I would like to re-use the same HttpClient instance for all requests,
//  but I need to somehow close it when the plugin is unloaded.

class HttpPlugin<TConfig>(config: TConfig) : Module<TConfig>(config) {

    override suspend fun get(args: ArgsGet, invoker: Invoker): HttpResponse? {
        return request(HttpMethod.Get, args.url, args.request)
    }

    override suspend fun post(args: ArgsPost, invoker: Invoker): HttpResponse? {
        return request(HttpMethod.Post, args.url, args.request)
    }

    private suspend fun request(httpMethod: HttpMethod, url: String, request: HttpRequest?): HttpResponse {
        val client = HttpClient() {
            install(HttpTimeout)
            engine {
                threadsCount = 1
            }
        }
        val response: KtorHttpResponse = client.use {
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
                    requestTimeoutMillis = request?.timeout?.toLong() ?: 0
                }
            }
        }

        val responseBody: String = if (response.contentType() == ContentType.Text.Any) {
            response.bodyAsText()
        } else {
            response.readBytes().encodeBase64()
        }

        return HttpResponse(
            status = response.status.value,
            statusText = response.status.description,
            headers = response.headers.toMap().mapValues { it.toString() },
            body = responseBody
        )
    }
}