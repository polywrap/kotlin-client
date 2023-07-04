package client.typeTestCases

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class InvalidTypesTestCase {

    private val uri = Uri("fs/$pathToTestWrappers/invalid-type/implementations/rs")
    private val client = ConfigBuilder().addDefaults().build()

    @Test
    fun testInvalidBoolIntSent() = runTest {
        val result = client.invoke<Boolean>(
            uri = uri,
            method = "boolMethod",
            args = mapOf(
                "arg" to 10
            )
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "Property must be of type 'bool'. Found 'int'.")
    }

    @Test
    fun testInvalidIntBoolSent() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "intMethod",
            args = mapOf(
                "arg" to true
            )
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "Property must be of type 'int'. Found 'bool'.")
    }

    @Test
    fun testInvalidUIntArraySent() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "uIntMethod",
            args = mapOf(
                "arg" to listOf(10)
            )
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "Property must be of type 'uint'. Found 'array'.")
    }

    @Test
    fun testInvalidBytesFloatSent() = runTest {
        val result = client.invoke<ByteArray>(
            uri = uri,
            method = "bytesMethod",
            args = mapOf(
                "arg" to 10.15
            )
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "Property must be of type 'bytes'. Found 'float64'.")
    }

    @Test
    fun testInvalidArrayMapSent() = runTest {
        val result = client.invoke<Array<String>>(
            uri = uri,
            method = "arrayMethod",
            args = mapOf(
                "arg" to mapOf("prop" to "prop")
            )
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "Property must be of type 'array'. Found 'map'.")
    }
}
