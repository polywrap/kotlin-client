package errorReproduction

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.configBuilder.DefaultBundle
import io.polywrap.configBuilder.polywrapClient
import io.polywrap.core.resolution.Uri
import io.polywrap.plugins.http.httpPlugin
import io.polywrap.plugins.http.wrap.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import memoryStoragePlugin
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals

// These tests are meant to reproduce a bug in the Kotlin client where subsequent
// invokes of the same plugin fail. This is due to an issue with the UniFFI handlemap.

@OptIn(ExperimentalCoroutinesApi::class)
class PluginHandlemapBug {

    @Test
    fun testSubsequentInvokes() = runTest {
        val uri = Uri("fs/$pathToTestWrappers/asyncify/implementations/rs")
        val client = ConfigBuilder()
            .addBundle(DefaultBundle.System)
            .setPackage(
                "wrap://ens/memory-storage.polywrap.eth"
                        to memoryStoragePlugin(null)
            )
            .build()

        val result = client.invoke<List<String>>(
            uri = uri,
            method = "subsequentInvokes",
            args = mapOf("numberOfTimes" to 40)
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val expected = List(40) { it.toString() }
        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun invokeHttpPluginByClient() {
        val uri = Uri("plugin/http@1.1.0")
        val plugin = httpPlugin(null)

        val client = polywrapClient {
            setPackage(uri.toString() to plugin)
        }

        val firstInvoke = client.invoke<Response?>(
            uri = uri,
            method = "get",
            args = mapOf("url" to "https://httpbin.org/get")
        )
        if (firstInvoke.isFailure) {
            throw Exception("FIRST INVOKE: ${firstInvoke.exceptionOrNull()!!.message}")
        }

        val secondInvoke = client.invoke<Response?>(
            uri = uri,
            method = "get",
            args = mapOf("url" to "https://httpbin.org/get")
        )
        if (secondInvoke.isFailure) {
            throw Exception("SECOND INVOKE: ${secondInvoke.exceptionOrNull()!!.message}")
        }


        val thirdInvoke = client.invoke<Response?>(
            uri = uri,
            method = "get",
            args = mapOf("url" to "https://httpbin.org/get")
        )
        if (thirdInvoke.isFailure) {
            throw Exception("THIRD INVOKE: ${thirdInvoke.exceptionOrNull()!!.message}")
        }

        val response = thirdInvoke.getOrThrow()
        assertEquals(200, response?.status)
    }
}
