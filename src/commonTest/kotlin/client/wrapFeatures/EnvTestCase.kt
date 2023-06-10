package client.wrapFeatures

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class EnvTestCase {

    private val externalWrapperUri = Uri.fromString("fs/$pathToTestWrappers/env-type/00-external/implementations/rs")
    private val wrapperUri = Uri.fromString("fs/$pathToTestWrappers/env-type/01-main/implementations/rs")

    private val envs = mapOf(
        wrapperUri.toStringUri() to mapOf(
            "object" to mapOf("prop" to "object string"),
            "str" to "string",
            "optFilledStr" to "optional string",
            "number" to 10,
            "bool" to true,
            "en" to "FIRST",
            "array" to listOf(32, 23)
        ),
        externalWrapperUri.toStringUri() to mapOf(
            "externalArray" to listOf(1, 2, 3),
            "externalString" to "iamexternal"
        )
    )

    private val client = ConfigBuilder()
        .addDefaults()
        .addEnvs(envs)
        .addRedirect("ens/external-env.polywrap.eth" to externalWrapperUri.toStringUri())
        .build()

    @Test
    fun testMethodRequireEnv() = runTest {
        val methodRequireEnvResult = client.invoke<Map<String, Any>>(
            uri = wrapperUri,
            method = "methodRequireEnv",
            args = mapOf("arg" to "string")
        )
        assertNull(methodRequireEnvResult.exceptionOrNull())
        assertEquals(
            mapOf(
                "str" to "string",
                "optFilledStr" to "optional string",
                "optStr" to null,
                "number" to 10.toByte(),
                "optNumber" to null,
                "bool" to true,
                "optBool" to null,
                "object" to mapOf("prop" to "object string"),
                "optObject" to null,
                "en" to 0.toByte(),
                "optEnum" to null,
                "array" to listOf<Byte>(32, 23)
            ),
            methodRequireEnvResult.getOrThrow()
        )
    }

    @Test
    fun testSubinvokeEnvMethod() = runTest {
        val subinvokeEnvMethodResult = client.invoke<Map<String, Any>>(
            uri = wrapperUri,
            method = "subinvokeEnvMethod",
            args = mapOf("arg" to "string")
        )
        assertNull(subinvokeEnvMethodResult.exceptionOrNull())
        assertEquals(
            mapOf(
                "local" to mapOf(
                    "str" to "string",
                    "optFilledStr" to "optional string",
                    "optStr" to null,
                    "number" to 10.toByte(),
                    "optNumber" to null,
                    "bool" to true,
                    "optBool" to null,
                    "object" to mapOf("prop" to "object string"),
                    "optObject" to null,
                    "en" to 0.toByte(),
                    "optEnum" to null,
                    "array" to listOf<Byte>(32, 23)
                ),
                "external" to mapOf(
                    "externalArray" to listOf<Byte>(1, 2, 3),
                    "externalString" to "iamexternal"
                )
            ),
            subinvokeEnvMethodResult.getOrThrow()
        )
    }

    @Test
    fun testMockUpdatedEnv() = runTest {
        val mockUpdatedEnvResult = client.invoke<Map<String, Any>>(
            uri = wrapperUri,
            method = "methodRequireEnv",
            args = mapOf("arg" to "string"),
            env = mapOf(
                "object" to mapOf("prop" to "object another string"),
                "str" to "another string",
                "optFilledStr" to "optional string",
                "number" to 10.toByte(),
                "bool" to true,
                "en" to "FIRST",
                "array" to listOf<Byte>(32, 23)
            )
        )
        assertNull(mockUpdatedEnvResult.exceptionOrNull())
        assertEquals(
            mapOf(
                "str" to "another string",
                "optFilledStr" to "optional string",
                "optStr" to null,
                "number" to 10.toByte(),
                "optNumber" to null,
                "bool" to true,
                "optBool" to null,
                "object" to mapOf("prop" to "object another string"),
                "optObject" to null,
                "en" to 0.toByte(),
                "optEnum" to null,
                "array" to listOf<Byte>(32, 23)
            ),
            mockUpdatedEnvResult.getOrThrow()
        )
    }
}
