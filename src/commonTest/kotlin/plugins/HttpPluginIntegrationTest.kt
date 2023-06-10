package plugins

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.configBuilder.DefaultBundle
import io.polywrap.core.msgpack.MsgPackMap
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.msgpack.toMsgPackMap
import io.polywrap.core.resolution.Uri
import io.polywrap.plugins.http.wrap.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class HttpPluginIntegrationTest {
    @Test
    fun shouldEncodeAndDecodeHttpRequestObject() {
        val httpRequest = ArgsGet(
            url = "https://ipfs.wrappers.io/api/v0/cat",
            request = Request(
                headers = null,
                urlParams = mapOf("arg" to "QmThRxFfr7Hj9Mq6WmcGXjkRrgqMG3oD93SLX27tinQWy5/wrap.info").toMsgPackMap(),
                responseType = ResponseType.BINARY,
                body = null,
                formData = null,
                timeout = 5000u
            )
        )

        val encoded = msgPackEncode(httpRequest)
        assertContentEquals(
            encoded,
            byteArrayOf(
                -126, -93, 117, 114, 108, -39, 35, 104, 116, 116, 112, 115, 58, 47, 47, 105, 112, 102, 115, 46, 119, 114, 97, 112, 112, 101, 114, 115, 46, 105, 111, 47, 97, 112, 105, 47, 118, 48, 47, 99, 97, 116, -89, 114, 101, 113, 117, 101, 115, 116, -122, -89, 104, 101, 97, 100, 101, 114, 115, -64, -87, 117, 114, 108, 80, 97, 114, 97, 109, 115, -57, 63, 1, -127, -93, 97, 114, 103, -39, 56, 81, 109, 84, 104, 82, 120, 70, 102, 114, 55, 72, 106, 57, 77, 113, 54, 87, 109, 99, 71, 88, 106, 107, 82, 114, 103, 113, 77, 71, 51, 111, 68, 57, 51, 83, 76, 88, 50, 55, 116, 105, 110, 81, 87, 121, 53, 47, 119, 114, 97, 112, 46, 105, 110, 102, 111, -84, 114, 101, 115, 112, 111, 110, 115, 101, 84, 121, 112, 101, 1, -92, 98, 111, 100, 121, -64, -88, 102, 111, 114, 109, 68, 97, 116, 97, -64, -89, 116, 105, 109, 101, 111, 117, 116, -51, 19, -120
            )
        )
        val decoded = msgPackDecode<ArgsGet>(encoded).getOrThrow()
        assertEquals(httpRequest, decoded)
    }

    @Test
    fun shouldEncodeAndDecodeHttpResponseObject() {
        val httpResponse = Response(
            status = 200,
            statusText = "OK",
            headers = MsgPackMap(
                map = mapOf(
                    "Date" to "Thu, 27 Apr 2023 07:34:18 GMT",
                    "Content-Type" to "application/json",
                    "Content-Length" to "288",
                    "Connection" to "keep-alive",
                    "Server" to "gunicorn/19.9.0",
                    "Access-Control-Allow-Origin" to "*",
                    "Access-Control-Allow-Credentials" to "true"
                )
            ),
            body = """{
            "args": {},
            "headers": {
            "Accept": "*/*",
            "Accept-Charset": "UTF-8",
            "Host": "httpbin.org",
            "User-Agent": "Ktor client",
            "X-Amzn-Trace-Id": "Root=1-644a257a-1633809912f132a53fa082bc"
        },
            "origin": "160.176.142.175",
            "url": "http://httpbin.org/get"
        }
            """.trimIndent()
        )

        val encoded = msgPackEncode(httpResponse)
        val decoded = msgPackDecode<Response?>(encoded).getOrThrow()
        assertEquals(httpResponse, decoded)
    }

    @Test
    fun invokeByClient() {
        val client = ConfigBuilder().addDefaults().build()
        val result = client.invoke<Response?>(
            uri = Uri.fromString(DefaultBundle.plugins["http"]!!.uri),
            method = "get",
            args = mapOf("url" to "https://httpbin.org/get")
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val response = result.getOrThrow()
        assertEquals(200, response?.status)
    }
}
