package client.wrapFeatures

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.Uri
import io.polywrap.uriResolvers.SequentialResolver
import io.polywrap.uriResolvers.embedded.PackageRedirectResolver
import io.polywrap.uriResolvers.embedded.UriRedirectResolver
import io.polywrap.uriResolvers.ExtendableUriResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mockPlugin
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

        val config = ConfigBuilder()
            .addDefaults()
            .addInterfaceImplementation(interfaceUri.uri, implementationUri.uri)
            .build()
        val client = PolywrapClient(config)

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

    @Test
    fun `should register interface implementations successfully`() = runTest {
        val interfaceUri = Uri("wrap://ens/some-interface1.eth")
        val implementation1Uri = Uri("wrap://ens/some-implementation1.eth")
        val implementation2Uri = Uri("wrap://ens/some-implementation2.eth")

        val config = ConfigBuilder()
            .addInterfaceImplementations(
                interfaceUri.uri,
                listOf(implementation1Uri.uri, implementation2Uri.uri)
            )
            .addResolver(
                UriRedirectResolver(
                    from = Uri("uri/foo"),
                    to = Uri("uri/bar")
                )
            )
            .build()
        val client = PolywrapClient(config)

        val interfaces = client.getInterfaces()
        assertEquals(
            mapOf(interfaceUri to listOf(implementation1Uri, implementation2Uri)),
            interfaces
        )

        val implementations = client.getImplementations(interfaceUri, applyResolution = false)
        assertNull(implementations.exceptionOrNull())
        assertEquals(
            listOf(implementation1Uri, implementation2Uri),
            implementations.getOrThrow()
        )
    }

    @Test
    fun `should get all implementations of interface`() = runTest {
        val interface1Uri = Uri("wrap://ens/some-interface1.eth")
        val interface2Uri = Uri("wrap://ens/some-interface2.eth")
        val interface3Uri = Uri("wrap://ens/some-interface3.eth")

        val implementation1Uri = Uri("wrap://ens/some-implementation.eth")
        val implementation2Uri = Uri("wrap://ens/some-implementation2.eth")
        val implementation3Uri = Uri("wrap://ens/some-implementation3.eth")
        val implementation4Uri = Uri("wrap://ens/some-implementation4.eth")

        val config = ConfigBuilder()
            .addInterfaceImplementations(
                interface1Uri.uri,
                listOf(implementation1Uri.uri, implementation2Uri.uri)
            )
            .addInterfaceImplementations(
                interface2Uri.uri,
                listOf(implementation3Uri.uri)
            )
            .addInterfaceImplementations(
                interface3Uri.uri,
                listOf(implementation3Uri.uri, implementation4Uri.uri)
            )
            .addResolver(
                SequentialResolver(
                    listOf(
                        UriRedirectResolver(from = interface1Uri, to = interface2Uri),
                        UriRedirectResolver(from = implementation1Uri, to = implementation2Uri),
                        UriRedirectResolver(from = implementation2Uri, to = implementation3Uri),
                        PackageRedirectResolver(implementation4Uri, mockPlugin(null))
                    )
                )
            )
            .build()
        val client = PolywrapClient(config)

        val implementations1 = client.getImplementations(interface1Uri, applyResolution = true).getOrThrow()
        val implementations2 = client.getImplementations(interface2Uri, applyResolution = true).getOrThrow()
        val implementations3 = client.getImplementations(interface3Uri, applyResolution = true).getOrThrow()

        assertEquals(
            listOf(implementation1Uri, implementation2Uri, implementation3Uri),
            implementations1
        )

        assertEquals(
            listOf(implementation1Uri, implementation2Uri, implementation3Uri),
            implementations2
        )

        assertEquals(
            listOf(implementation3Uri, implementation4Uri),
            implementations3
        )
    }

    @Test
    fun `should merge user-defined interface implementations with each other`() = runTest {
        val interfaceUri = Uri("wrap://ens/interface.eth")
        val implementationUri1 = Uri("wrap://ens/implementation1.eth")
        val implementationUri2 = Uri("wrap://ens/implementation2.eth")

        val config = ConfigBuilder()
            .addDefaults()
            .addInterfaceImplementations(
                interfaceUri.uri,
                listOf(implementationUri1.uri, implementationUri2.uri)
            )
            .build()
        val client = PolywrapClient(config)

        val implementationUris = client.getInterfaces()?.get(interfaceUri)

        assertEquals(
            listOf(implementationUri1, implementationUri2),
            implementationUris
        )
    }

    @Test
    fun `should merge user-defined interface implementations with defaults`() = runTest {
        val interfaceUri = ExtendableUriResolver.defaultExtInterfaceUris[0]
        val implementationUri1 = Uri("wrap://ens/implementation1.eth")
        val implementationUri2 = Uri("wrap://ens/implementation2.eth")

        val config = ConfigBuilder()
            .addDefaults()
            .addInterfaceImplementations(
                interfaceUri.uri,
                listOf(implementationUri1.uri, implementationUri2.uri)
            )
            .build()
        val client = PolywrapClient(config)

        val implementationUris = client.getInterfaces()?.get(interfaceUri)

        val builder = ConfigBuilder()
        val defaultClientConfig = builder.addDefaults().build()

        assertEquals(
            (
                (defaultClientConfig.interfaces?.get(interfaceUri) ?: emptyList()) + listOf(
                    implementationUri1,
                    implementationUri2
                )
                ),
            implementationUris
        )
    }

    @Test
    fun `get implementations - do not return plugins that are not explicitly registered`() = runTest {
        val interfaceUri = Uri("wrap://ens/some-interface.eth")
        val implementation1Uri = Uri("wrap://ens/some-implementation1.eth")
        val implementation2Uri = Uri("wrap://ens/some-implementation2.eth")

        val config = ConfigBuilder()
            .addInterfaceImplementation(interfaceUri.uri, implementation2Uri.uri)
            .addResolver(PackageRedirectResolver(implementation1Uri, mockPlugin(null)))
            .build()
        val client = PolywrapClient(config)

        val getImplementationsResult = client.getImplementations(interfaceUri, applyResolution = true).getOrThrow()

        assertEquals(
            listOf(implementation2Uri),
            getImplementationsResult
        )
    }

    @Test
    fun `get implementations - return implementations for plugins which don't have interface stated in manifest`() = runTest {
        val interfaceUri = Uri("wrap://ens/some-interface.eth")
        val implementation1Uri = Uri("wrap://ens/some-implementation1.eth")
        val implementation2Uri = Uri("wrap://ens/some-implementation2.eth")

        val config = ConfigBuilder()
            .addInterfaceImplementations(
                interfaceUri.uri,
                listOf(implementation1Uri.uri, implementation2Uri.uri)
            )
            .addResolver(PackageRedirectResolver(implementation1Uri, mockPlugin(null)))
            .build()
        val client = PolywrapClient(config)

        val getImplementationsResult = client.getImplementations(interfaceUri, applyResolution = true).getOrThrow()

        assertEquals(
            listOf(implementation1Uri, implementation2Uri),
            getImplementationsResult
        )
    }
}
