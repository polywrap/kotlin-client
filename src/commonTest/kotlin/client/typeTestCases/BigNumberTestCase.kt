package client.typeTestCases

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BigNumberTestCase {

    private val uri = Uri.fromString("fs/$pathToTestWrappers/bignumber-type/implementations/rs")
    private val client = ConfigBuilder().addDefaults().build()

    @Test
    fun testBigNumberTypeSimple() = runTest {
        val arg1 = "1234.56789123456789".toBigDecimal()
        val prop1 = "98.7654321987654321".toBigDecimal()

        val result = client.invoke<String>(
            uri = uri,
            method = "method",
            args = mapOf(
                "arg1" to arg1.toStringExpanded(),
                "obj" to mapOf(
                    "prop1" to prop1.toStringExpanded()
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val expected = (arg1 * prop1).toStringExpanded()
        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun testBigNumberTypeComplex() = runTest {
        val arg1 = "1234567.89123456789".toBigDecimal()
        val prop1 = "987654.321987654321".toBigDecimal()
        val arg2 = "123456789123.456789123456789123456789".toBigDecimal()
        val prop2 = "987.654321987654321987654321987654321".toBigDecimal()

        val result = client.invoke<String>(
            uri = uri,
            method = "method",
            args = mapOf(
                "arg1" to arg1.toStringExpanded(),
                "arg2" to arg2.toStringExpanded(),
                "obj" to mapOf(
                    "prop1" to prop1.toStringExpanded(),
                    "prop2" to prop2.toStringExpanded()
                )
            )
        )
        if (result.isFailure) throw result.exceptionOrNull()!!

        val expected = ((arg1 * arg2) * (prop1 * prop2)).toStringExpanded()
        assertEquals(expected, result.getOrThrow())
    }
}
