package client.typeTestCases

import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ClientConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BigIntTestCase {

    private val uri = Uri("fs/$pathToTestWrappers/bigint-type/implementations/rs")
    private val config = ClientConfigBuilder().addDefaults().build()
    private val client = PolywrapClient(config)

    @Test
    fun testBigIntTypeSimple() = runTest {
        val result = client.invoke<String>(
            uri = uri,
            method = "method",
            args = mapOf(
                "arg1" to "123456789123456789",
                "obj" to mapOf(
                    "prop1" to "987654321987654321"
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val expected = "123456789123456789".toBigInteger() * "987654321987654321".toBigInteger()
        assertEquals(expected.toString(), result.getOrThrow())
    }

    @Test
    fun testBigIntTypeComplex() = runTest {
        val result = client.invoke<String>(
            uri = uri,
            method = "method",
            args = mapOf(
                "arg1" to "123456789123456789",
                "arg2" to "123456789123456789123456789123456789",
                "obj" to mapOf(
                    "prop1" to "987654321987654321",
                    "prop2" to "987654321987654321987654321987654321"
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val expected = "123456789123456789".toBigInteger() *
            "123456789123456789123456789123456789".toBigInteger() *
            "987654321987654321".toBigInteger() *
            "987654321987654321987654321987654321".toBigInteger()
        assertEquals(expected.toString(), result.getOrThrow())
    }
}
