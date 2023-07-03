package client

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.InvokeResult
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.resolution.Uri
import kotlinx.serialization.Serializable
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNull

class SanityClientTest {

    private val sha3Uri = Uri("ipfs/QmThRxFfr7Hj9Mq6WmcGXjkRrgqMG3oD93SLX27tinQWy5")

    @Test
    fun invokeRaw() {
        val client = ConfigBuilder().addDefaults().build()
        val result = client.invokeRaw(
            uri = sha3Uri,
            method = "keccak_256",
            args = msgPackEncode(mapOf("message" to "Hello World!"))
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertNull(result.exceptionOrNull())
    }

    @Ignore
    @Test
    fun invokeWithMapStringAnyArgs() {
        val client = ConfigBuilder().addDefaults().build()
        val result = client.invoke<String>(
            uri = sha3Uri,
            method = "keccak_256",
            args = mapOf("message" to "Hello World!")
        )
        assertNull(result.exceptionOrNull())
    }

    @Ignore
    @Test
    fun invokeWithReifiedTypes() {
        @Serializable
        data class Keccak256Args(val message: String)

        val client = ConfigBuilder().addDefaults().build()
        val result: InvokeResult<String> = client.invoke(
            uri = sha3Uri,
            method = "keccak_256",
            args = Keccak256Args("Hello World!")
        )
        assertNull(result.exceptionOrNull())
    }
}
