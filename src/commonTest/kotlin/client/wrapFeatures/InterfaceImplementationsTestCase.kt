package client.wrapFeatures

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.resolution.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InterfaceImplementationsTestCase {

    @Test
    fun invokeInterfaceWrappers() = runTest {
        val interfaceUri = Uri("wrap://ens/interface.eth")
        val implementationUri = Uri("fs/$pathToTestWrappers/interface-invoke/01-implementation/implementations/rs")
        val wrapperUri = Uri("fs/$pathToTestWrappers/interface-invoke/02-wrapper/implementations/rs")

        val client = ConfigBuilder()
            .addDefaults()
            .addInterfaceImplementation(interfaceUri.toString(), implementationUri.toString())
            .build()

        val result = client.invoke<Map<String, Any>>(
            uri = wrapperUri,
            method = "moduleMethod",
            args = mapOf(
                "arg" to mapOf(
                    "uint8" to 1,
                    "str" to "Test String 1"
                )
            )
        )

        assertNull(result.exceptionOrNull())
        assertEquals(
            mapOf(
                "uint8" to 1.toByte(),
                "str" to "Test String 1"
            ),
            result.getOrThrow()
        )
    }
}
