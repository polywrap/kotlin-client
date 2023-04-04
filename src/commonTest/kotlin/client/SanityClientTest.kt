package client

import eth.krisbitney.polywrap.client.PolywrapClient
import eth.krisbitney.polywrap.configBuilder.ClientConfigBuilder
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertNull

class SanityClientTest {

    private val sha3Uri = Uri("ipfs/QmThRxFfr7Hj9Mq6WmcGXjkRrgqMG3oD93SLX27tinQWy5")

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun tryResolveUri() = runTest {
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.tryResolveUri(uri = sha3Uri).await()

        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun invoke() = runTest {
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.invoke(
            InvokeOptions(
                uri = sha3Uri,
                method = "keccak_256",
                args = msgPackEncode(mapOf("message" to "Hello World!"))
            )
        ).await()
        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun invokeWithMapStringAnyArgs() = runTest {
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val result = client.invoke<String>(
            uri = sha3Uri,
            method = "keccak_256",
            args = mapOf("message" to "Hello World!")
        ).await()
        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun invokeWithReifiedTypes() = runTest {
        @Serializable
        data class CustomObject(
            val firstKey: String,
            val secondKey: String
        )
        val config = ClientConfigBuilder().addDefaults().build()
        val client = PolywrapClient(config)
        val deferred: Deferred<Result<String>> = client.invoke(
            uri = sha3Uri,
            method = "keccak_256",
            args = CustomObject("firstValue", "secondValue")
        )
        val result = deferred.await()
        assertNull(result.exceptionOrNull())
        println(result.getOrThrow())
    }
}
