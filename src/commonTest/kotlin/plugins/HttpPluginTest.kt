package plugins

import emptyMockInvoker
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.polywrap.msgpack.toMsgPackMap
import io.polywrap.plugins.http.HttpPlugin
import io.polywrap.plugins.http.wrapHardCoded.ArgsGet
import io.polywrap.plugins.http.wrapHardCoded.ArgsPost
import io.polywrap.plugins.http.wrapHardCoded.HttpRequest
import io.polywrap.plugins.http.wrapHardCoded.HttpResponseType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HttpPluginTest {

    private val httpClient = HttpClient(MockEngine) {
        install(HttpTimeout)
        engine {
            threadsCount = 1
            addHandler { request ->
                val fullUrl = request.url.toString()
                val baseUrl = fullUrl.split("?")[0]
                when (baseUrl) {
                    "https://example.com/success-text" -> {
                        if (fullUrl.contains("param1=value1") && fullUrl.contains("param2=value2")) {
                            // Check if the request contains the required headers
                            if (request.headers["X-Test-Header"] == "test-value") {
                                respond(
                                    content = "Success",
                                    status = HttpStatusCode.OK,
                                    headers = headersOf("Content-Type" to listOf("text/plain"))
                                )
                            } else {
                                error("Request is missing required headers")
                            }
                        } else {
                            respond(
                                content = "Success",
                                status = HttpStatusCode.OK,
                                headers = headersOf("Content-Type" to listOf("text/plain"))
                            )
                        }
                    }
                    "https://example.com/success-binary" -> {
                        respond(
                            content = ByteArray(4) { 0x42 },
                            status = HttpStatusCode.OK,
                            headers = headersOf("Content-Type" to listOf("application/octet-stream"))
                        )
                    }
                    "https://example.com/failure" -> {
                        respond("", status = HttpStatusCode.InternalServerError)
                    }
                    else -> error("Unhandled request: $request")
                }
            }
        }
    }

    private val httpPlugin = HttpPlugin(HttpPlugin.Config(httpClient))

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulGetRequestWithResponseTypeText() = runTest {
        val args = ArgsGet(
            url = "https://example.com/success-text",
            request = HttpRequest(responseType = HttpResponseType.TEXT)
        )
        val response = httpPlugin.get(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.OK.value, response.status)
        assertEquals("Success", response.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulGetRequestWithResponseTypeBinary() = runTest {
        val args = ArgsGet(
            url = "https://example.com/success-binary",
            request = HttpRequest(responseType = HttpResponseType.BINARY)
        )
        val response = httpPlugin.get(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.OK.value, response.status)
        assertEquals("QkJCQg==", response.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulGetRequestWithQueryParamsAndRequestHeaders() = runTest {
        val args = ArgsGet(
            url = "https://example.com/success-text",
            request = HttpRequest(
                headers = mapOf("X-Test-Header" to "test-value").toMsgPackMap(),
                urlParams = mapOf("param1" to "value1", "param2" to "value2").toMsgPackMap(),
                responseType = HttpResponseType.TEXT
            )
        )
        val response = httpPlugin.get(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.OK.value, response.status)
        assertEquals("Success", response.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun failedGetRequest() = runTest {
        val args = ArgsGet(
            url = "https://example.com/failure",
            request = HttpRequest(responseType = HttpResponseType.TEXT)
        )
        val response = httpPlugin.get(args, emptyMockInvoker)
        assertNotNull(response)
        assertEquals(HttpStatusCode.InternalServerError.value, response.status)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulPostRequestWithContentTypeApplicationJson() = runTest {
        val args = ArgsPost(
            url = "https://example.com/success-text",
            request = HttpRequest(
                headers = mapOf("Content-Type" to "application/json").toMsgPackMap(),
                responseType = HttpResponseType.TEXT,
                body = """{"key": "value"}"""
            )
        )
        val response = httpPlugin.post(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.OK.value, response.status)
        assertEquals("Success", response.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulPostRequestWithResponseTypeText() = runTest {
        val args = ArgsPost(
            url = "https://example.com/success-text",
            request = HttpRequest(
                responseType = HttpResponseType.TEXT,
                body = "Hello World"
            )
        )
        val response = httpPlugin.post(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.OK.value, response.status)
        assertEquals("Success", response.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulPostRequestWithResponseTypeBinary() = runTest {
        val args = ArgsPost(
            url = "https://example.com/success-binary",
            request = HttpRequest(
                responseType = HttpResponseType.BINARY,
                body = "Hello World"
            )
        )
        val response = httpPlugin.post(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.OK.value, response.status)
        assertEquals("QkJCQg==", response.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulPostRequestWithQueryParamsAndRequestHeaders() = runTest {
        val args = ArgsPost(
            url = "https://example.com/success-text",
            request = HttpRequest(
                headers = mapOf("X-Test-Header" to "test-value").toMsgPackMap(),
                urlParams = mapOf("param1" to "value1", "param2" to "value2").toMsgPackMap(),
                responseType = HttpResponseType.TEXT,
                body = "Hello World"
            )
        )
        val response = httpPlugin.post(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.OK.value, response.status)
        assertEquals("Success", response.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun failedPostRequest() = runTest {
        val args = ArgsPost(
            url = "https://example.com/failure",
            request = HttpRequest(
                responseType = HttpResponseType.TEXT,
                body = "Hello World"
            )
        )
        val response = httpPlugin.post(args, emptyMockInvoker)

        assertNotNull(response)
        assertEquals(HttpStatusCode.InternalServerError.value, response.status)
    }
}
