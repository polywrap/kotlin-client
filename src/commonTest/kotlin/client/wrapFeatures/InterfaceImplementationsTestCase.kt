package client.wrapFeatures

import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.uriResolvers.StaticResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pathToTestWrappers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InterfaceImplementationsTestCase {

    @Test
    fun invokeInterfaceWrappers() = runTest {
        val interfaceUri = Uri.fromString("wrap://ens/interface.eth")
        val implementationUri = Uri.fromString("fs/$pathToTestWrappers/interface-invoke/01-implementation/implementations/rs")
        val wrapperUri = Uri.fromString("fs/$pathToTestWrappers/interface-invoke/02-wrapper/implementations/rs")

        val client = ConfigBuilder()
            .addDefaults()
            .addInterfaceImplementation(interfaceUri.toStringUri(), implementationUri.toStringUri())
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

        interfaceUri.close()
        implementationUri.close()
        wrapperUri.close()
    }

    @Test
    fun `should register interface implementations successfully`() = runTest {
        val interfaceUri = "wrap://ens/some-interface1.eth"
        val implementation1Uri = "wrap://ens/some-implementation1.eth"
        val implementation2Uri = "wrap://ens/some-implementation2.eth"

        val client = ConfigBuilder()
            .addInterfaceImplementations(
                interfaceUri,
                listOf(implementation1Uri, implementation2Uri)
            )
            .addResolver(
                StaticResolver(
                    mapOf(
                        "uri/foo" to UriPackageOrWrapper.UriValue(Uri.fromString("uri/bar"))
                    )
                )
            )
            .build()

        val interfaces = client.getInterfaces()
        assertEquals(
            mapOf(interfaceUri to listOf(implementation1Uri, implementation2Uri)),
            interfaces
        )

        val implementations = client.getImplementations(interfaceUri).getOrNull()
        assertNotNull(implementations)
        assertEquals(
            listOf(implementation1Uri, implementation2Uri),
            implementations
        )
    }
}
