package client

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ClientConfigBuilder
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.types.InvokeOptions
import io.polywrap.msgpack.msgPackEncode
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SanityClientTest {

    private val sha3Uri = Uri("ipfs/QmThRxFfr7Hj9Mq6WmcGXjkRrgqMG3oD93SLX27tinQWy5")

    @Test
    fun tryResolveUri() {
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.tryResolveUri(uri = sha3Uri)

        assertNull(result.exceptionOrNull())
        assertTrue(result.getOrThrow() is UriPackageOrWrapper.WrapperValue)
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
    }

    @Test
    fun invokeWithReifiedTypes() {
        @Serializable
        data class Keccak256Args(val message: String)

        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.invoke<Keccak256Args, String>(
            uri = sha3Uri,
            method = "keccak_256",
            args = Keccak256Args("Hello World!")
        )
        assertNull(result.exceptionOrNull())
    }
}
