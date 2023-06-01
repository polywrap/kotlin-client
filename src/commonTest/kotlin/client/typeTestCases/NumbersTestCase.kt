package client.typeTestCases

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class NumbersTestCase {

    private val uri = Uri("fs/$pathToTestWrappers/numbers-type/implementations/rs")
    private val config = ConfigBuilder().addDefaults().build()
    private val client = PolywrapClient(config)

    @Test
    fun testI8Underflow() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "i8Method",
            args = mapOf("first" to -129, "second" to 10)
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "integer overflow: value = -129; bits = 8")
    }

    @Test
    fun testU8Overflow() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "u8Method",
            args = mapOf("first" to 256, "second" to 10)
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "unsigned integer overflow: value = 256; bits = 8")
    }

    @Test
    fun testI16Underflow() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "i16Method",
            args = mapOf("first" to -32769, "second" to 10)
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "integer overflow: value = -32769; bits = 16")
    }

    @Test
    fun testU16Overflow() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "u16Method",
            args = mapOf("first" to 65536, "second" to 10)
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "unsigned integer overflow: value = 65536; bits = 16")
    }

    @Test
    fun testI32Underflow() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "i32Method",
            args = mapOf("first" to -2147483649, "second" to 10)
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "integer overflow: value = -2147483649; bits = 32")
    }

    @Test
    fun testU32Overflow() = runTest {
        val result = client.invoke<Int>(
            uri = uri,
            method = "u32Method",
            args = mapOf("first" to 4294967296, "second" to 10)
        )
        assertFalse(result.isSuccess, "An exception should have been thrown.")
        assertContains(result.exceptionOrNull()!!.message!!, "unsigned integer overflow: value = 4294967296; bits = 32")
    }
}
