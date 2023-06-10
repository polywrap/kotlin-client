package client.typeTestCases

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectTestCase {

    private val uri = Uri.fromString("fs/$pathToTestWrappers/object-type/implementations/rs")
    private val client = ConfigBuilder().addDefaults().build()

    @Serializable
    private data class Nested(val prop: String)

    @Serializable
    private data class Output(val prop: String, val nested: Nested)

    @Test
    fun testMethod1a() = runTest {
        val result = client.invoke<List<Output>>(
            uri = uri,
            method = "method1",
            args = mapOf(
                "arg1" to mapOf(
                    "prop" to "arg1 prop",
                    "nested" to mapOf("prop" to "arg1 nested prop")
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        assertEquals(
            listOf(
                Output("arg1 prop", Nested("arg1 nested prop")),
                Output("", Nested(""))
            ),
            result.getOrThrow()
        )
    }

    @Test
    fun testMethod1b() = runTest {
        val result = client.invoke<List<Output>>(
            uri = uri,
            method = "method1",
            args = mapOf(
                "arg1" to mapOf(
                    "prop" to "arg1 prop",
                    "nested" to mapOf("prop" to "arg1 nested prop")
                ),
                "arg2" to mapOf(
                    "prop" to "arg2 prop",
                    "circular" to mapOf("prop" to "arg2 circular prop")
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        assertEquals(
            listOf(
                Output("arg1 prop", Nested("arg1 nested prop")),
                Output("arg2 prop", Nested("arg2 circular prop"))
            ),
            result.getOrThrow()
        )
    }

    @Test
    fun testMethod2a() = runTest {
        val result = client.invoke<Output?>(
            uri = uri,
            method = "method2",
            args = mapOf(
                "arg" to mapOf(
                    "prop" to "arg prop",
                    "nested" to mapOf("prop" to "arg nested prop")
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        assertEquals(
            Output("arg prop", Nested("arg nested prop")),
            result.getOrThrow()
        )
    }

    @Test
    fun testMethod2b() = runTest {
        val result = client.invoke<Output?>(
            uri = uri,
            method = "method2",
            args = mapOf(
                "arg" to mapOf(
                    "prop" to "null",
                    "nested" to mapOf("prop" to "arg nested prop")
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        assertEquals(null, result.getOrThrow())
    }

    @Test
    fun testMethod3() = runTest {
        val result = client.invoke<List<Output?>>(
            uri = uri,
            method = "method3",
            args = mapOf(
                "arg" to mapOf(
                    "prop" to "arg prop",
                    "nested" to mapOf("prop" to "arg nested prop")
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        assertEquals(
            listOf(
                null,
                Output("arg prop", Nested("arg nested prop"))
            ),
            result.getOrThrow()
        )
    }

    @Test
    fun testMethod4() = runTest {
        val result = client.invoke<Output>(
            uri = uri,
            method = "method5", // TODO: this is named method4 in newer versions of the wrap test harness
            args = mapOf(
                "arg" to mapOf(
                    "prop" to listOf(49, 50, 51, 52)
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        assertEquals(
            Output("1234", Nested("nested prop")),
            result.getOrThrow()
        )
    }
}
