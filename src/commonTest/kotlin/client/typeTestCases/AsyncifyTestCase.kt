package client.typeTestCases

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import memoryStoragePlugin
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncifyTestCase {

    private val uri = Uri("fs/$pathToTestWrappers/asyncify/implementations/rs")
    private val client = ConfigBuilder()
        .addDefaults()
        .addPackage(
            "wrap://ens/memory-storage.polywrap.eth"
                to memoryStoragePlugin(null)
        )
        .build()

    @Test
    fun testSubsequentInvokes() = runTest {
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
    fun testLocalVarMethod() = runTest {
        val result = client.invoke<Boolean>(
            uri = uri,
            method = "localVarMethod"
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertTrue(result.getOrThrow())
    }

    @Test
    fun testGlobalVarMethod() = runTest {
        val result = client.invoke<Boolean>(
            uri = uri,
            method = "globalVarMethod"
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertTrue(result.getOrThrow())
    }

    @Test
    fun testSetDataWithLargeArgs() = runTest {
        val largeStr = "polywrap ".repeat(10000)
        val result = client.invoke<String>(
            uri = uri,
            method = "setDataWithLargeArgs",
            args = mapOf("value" to largeStr)
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertEquals(largeStr, result.getOrThrow())
    }

    @Test
    fun testSetDataWithManyArgs() = runTest {
        val args = ('A'..'L').associate { "value$it" to "polywrap ${it.lowercase()}" }
        val result = client.invoke<String>(
            uri = uri,
            method = "setDataWithManyArgs",
            args = args
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val expectedResult = "polywrap " + ('a'..'k').joinToString("") { "${it}polywrap " } + "l"
        assertEquals(expectedResult, result.getOrThrow())
    }

    @Test
    fun testSetDataWithManyStructuredArgs() = runTest {
        fun createObj(i: Int): Map<String, String> {
            return ('A'..'L').associate { "prop$it" to "${it.lowercase()}-$i" }
        }

        val args = ('A'..'L')
            .mapIndexed { index, char -> "value$char" to createObj(index + 1) }
            .toMap()

        val result = client.invoke<Boolean>(
            uri = uri,
            method = "setDataWithManyStructuredArgs",
            args = args
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertTrue(result.getOrThrow())
    }
}
