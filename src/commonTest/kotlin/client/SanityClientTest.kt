package client

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.InvokeResult
import io.polywrap.core.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.types.InvokeOptions
import io.polywrap.core.msgpack.msgPackEncode
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SanityClientTest {

    private val sha3Uri = Uri("ipfs/QmThRxFfr7Hj9Mq6WmcGXjkRrgqMG3oD93SLX27tinQWy5")

    @Test
    fun tryResolveUri() {
        val config = ConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.tryResolveUri(uri = sha3Uri)

        assertNull(result.exceptionOrNull())
        assertTrue(result.getOrThrow() is UriPackageOrWrapper.WrapperValue)
    }

    @Test
    fun invoke() {
        val config = ConfigBuilder().addDefaults().build()
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
        val config = ConfigBuilder().addDefaults().build()
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

        val config = ConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result: InvokeResult<String> = client.invoke(
            uri = sha3Uri,
            method = "keccak_256",
            args = Keccak256Args("Hello World!")
        )
        assertNull(result.exceptionOrNull())
    }
}
