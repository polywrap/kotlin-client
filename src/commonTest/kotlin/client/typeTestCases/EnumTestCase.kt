package client.typeTestCases

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ClientConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class EnumTestCase {

    private val uri = Uri("fs/$pathToTestWrappers/enum-type/implementations/rs")
    private val config = ClientConfigBuilder().addDefaults().build()
    private val client = PolywrapClient(config)

    @Test
    fun testMethod1a() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "method1",
            args = mapOf(
                "en" to 5
            )
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "Invalid value for enum 'SanityEnum': 5")
    }

    @Test
    fun testMethod1b() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "method1",
            args = mapOf(
                "en" to 2,
                "optEnum" to 1
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!
        assertEquals(2, result.getOrThrow())
    }

    @Test
    fun testMethod1c() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "method1",
            args = mapOf(
                "en" to 1,
                "optEnum" to "INVALID"
            )
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "Invalid key for enum 'SanityEnum': INVALID")
    }

    @Test
    fun testMethod2a() = runTest {
        val result = client.invoke<List<Int>>(
            uri = uri,
            method = "method2",
            args = mapOf(
                "enumArray" to listOf(0, 0, 2)
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val expectedResult = listOf(0, 0, 2)
        assertEquals(expectedResult, result.getOrThrow())
    }
}
