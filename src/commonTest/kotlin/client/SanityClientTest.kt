package client

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ClientConfigBuilder
import io.polywrap.core.resolution.Uri
import io.polywrap.core.types.InvokeOptions
import io.polywrap.msgpack.msgPackEncode
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertNull

class SanityClientTest {

    private val sha3Uri = Uri("ipfs/QmThRxFfr7Hj9Mq6WmcGXjkRrgqMG3oD93SLX27tinQWy5")

    @Test
    fun tryResolveUri() {
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.tryResolveUri(uri = sha3Uri)

        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }

    @Test
    fun invoke() {
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.invoke(
            InvokeOptions(
                uri = sha3Uri,
                method = "keccak_256",
                args = msgPackEncode(mapOf("message" to "Hello World!"))
            )
        )
        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }

    @Test
    fun invokeWithMapStringAnyArgs() {
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.invoke<String>(
            uri = sha3Uri,
            method = "keccak_256",
            args = mapOf("message" to "Hello World!")
        )
        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }

    @Test
    fun invokeWithReifiedTypes() {
        @Serializable
        data class MethodArgs(
            val firstKey: String,
            val secondKey: String
        )
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.invoke<MethodArgs, String>(
            uri = sha3Uri,
            method = "keccak_256",
            args = MethodArgs("firstValue", "secondValue")
        )
        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }
}
