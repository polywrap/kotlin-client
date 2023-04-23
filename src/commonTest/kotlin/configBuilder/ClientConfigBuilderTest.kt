package configBuilder

import io.polywrap.configBuilder.BuilderConfig
import io.polywrap.configBuilder.ClientConfigBuilder
import io.polywrap.configBuilder.DefaultBundle
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.resolution.UriResolver
import io.polywrap.core.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class ClientConfigBuilderTest {

    private val emptyBuilderConfig = ClientConfigBuilder().config

    class MockUriResolver(val from: String, val to: String) : UriResolver {
        override fun tryResolveUri(
            uri: Uri,
            client: Client,
            resolutionContext: UriResolutionContext,
            resolveToPackage: Boolean
        ): Result<UriPackageOrWrapper> {
            throw NotImplementedError()
        }
    }

    private val mockWrapPackage: WrapPackage = object : WrapPackage {
        override fun createWrapper() = throw NotImplementedError()
        override fun getManifest() = throw NotImplementedError()
        override fun getFile(path: String): Result<ByteArray> {
            throw NotImplementedError()
        }
    }

    private val mockWrapper: Wrapper = object : Wrapper {
        override fun invoke(options: InvokeOptions, invoker: Invoker): Result<ByteArray> {
            throw NotImplementedError()
        }
    }

    private val testEnv1: Pair<String, WrapperEnv> = Pair(
        "wrap://ens/test.plugin.one",
        mapOf("test" to "value")
    )

    private val testEnv2: Pair<String, WrapperEnv> = Pair(
        "wrap://ens/test.plugin.two",
        mapOf("test" to "value")
    )

    private val testInterface1: Pair<String, MutableSet<String>> = Pair(
        "wrap://ens/test-interface-1.polywrap.eth",
        mutableSetOf("wrap://ens/test1.polywrap.eth")
    )

    private val testInterface2: Pair<String, MutableSet<String>> = Pair(
        "wrap://ens/test-interface-2.polywrap.eth",
        mutableSetOf("wrap://ens/test2.polywrap.eth")
    )

    private val testUriRedirect1 =
        "wrap://ens/test-one.polywrap.eth" to "wrap://ens/test1.polywrap.eth"

    private val testUriRedirect2 =
        "wrap://ens/test-two.polywrap.eth" to "wrap://ens/test2.polywrap.eth"

    val testEnvs: MutableMap<String, Map<String, Any>> = mutableMapOf(
        testEnv1,
        testEnv2
    )

    val testInterfaces: MutableMap<String, MutableSet<String>> = mutableMapOf(
        testInterface1,
        testInterface2
    )

    val testUriRedirects: MutableMap<String, String> = mutableMapOf(
        testUriRedirect1,
        testUriRedirect2
    )

    val testUriResolver: UriResolver = MockUriResolver(
        "wrap://ens/testFrom.eth",
        "wrap://ens/testTo.eth"
    )

    @Test
    fun shouldBuildAnEmptyPartialConfig() {
        val config = ClientConfigBuilder().build()
        assertEquals(config.interfaces, mapOf())
        assertEquals(config.envs, mapOf())
    }

    @Test
    fun shouldSuccessfullyAddConfigObjectAndBuild() {
        val configObject = BuilderConfig(
            testEnvs,
            testInterfaces,
            testUriRedirects,
            mutableMapOf(),
            mutableMapOf(),
            mutableListOf(testUriResolver)
        )

        val builder = ClientConfigBuilder().add(configObject)

        val clientConfig = builder.build()
        val builderConfig = builder.config

        assertEquals(
            mutableMapOf(
                Uri("wrap://ens/test.plugin.one") to mapOf("test" to "value"),
                Uri("wrap://ens/test.plugin.two") to mapOf("test" to "value")
            ),
            clientConfig.envs
        )

        assertEquals(
            mutableMapOf(
                Uri("wrap://ens/test-interface-1.polywrap.eth") to listOf(Uri("wrap://ens/test1.polywrap.eth")),
                Uri("wrap://ens/test-interface-2.polywrap.eth") to listOf(Uri("wrap://ens/test2.polywrap.eth"))
            ),
            clientConfig.interfaces
        )

        assertEquals(configObject, builderConfig)
    }

    @Test
    fun shouldSuccessfullyAddTheDefaultConfig() {
        val builder = ClientConfigBuilder().addDefaults()

        val clientConfig = builder.build()
        val builderConfig = builder.config

        assertNotNull(clientConfig)

        val expectedBuilderConfig = DefaultBundle.getConfig()
        assertEquals(expectedBuilderConfig, builderConfig)
    }

    @Test
    fun shouldSuccessfullyAddAnEnv() {
        val envUri = "wrap://ens/some-plugin.polywrap.eth"
        val env = mapOf(
            "foo" to "bar",
            "baz" to mapOf("biz" to "buz")
        )

        val config = ClientConfigBuilder().addEnv(envUri to env).build()

        assertNotNull(config.envs)
        assertEquals(1, config.envs!!.size)
        assertEquals(env, config.envs!![Uri(envUri)])
    }

    @Test
    fun shouldSuccessfullyAddToAnExistingEnv() {
        val envUri = "wrap://ens/some-plugin.polywrap.eth"
        val env1 = mapOf("foo" to "bar")
        val env2 = mapOf("baz" to mapOf("biz" to "buz"))

        val config = ClientConfigBuilder()
            .addEnv(envUri to env1)
            .addEnv(envUri to env2)
            .build()

        val expectedEnv = env1 + env2

        assertNotNull(config.envs)
        assertEquals(1, config.envs!!.size)
        assertEquals(expectedEnv, config.envs!![Uri(envUri)])
    }

    @Test
    fun shouldSuccessfullyAddTwoSeparateEnvs() {
        val config = ClientConfigBuilder()
            .addEnv(testEnvs.keys.first() to testEnvs.values.first())
            .addEnv(testEnvs.keys.last() to testEnvs.values.last())
            .build()

        assertNotNull(config.envs)
        assertEquals(2, config.envs!!.size)
        assertEquals(testEnvs.values.first(), config.envs!![Uri(testEnvs.keys.first())])
        assertEquals(testEnvs.values.last(), config.envs!![Uri(testEnvs.keys.last())])
    }

    @Test
    fun shouldRemoveAnEnv() {
        val config = ClientConfigBuilder()
            .addEnv(testEnvs.keys.first() to testEnvs.values.first())
            .addEnv(testEnvs.keys.last() to testEnvs.values.last())
            .removeEnv(testEnvs.keys.first())
            .build()

        assertNotNull(config.envs)
        assertEquals(1, config.envs!!.size)
        assertEquals(testEnvs.values.last(), config.envs!![Uri(testEnvs.keys.last())])
    }

    @Test
    fun shouldSetAnEnv() {
        val envUri = "wrap://ens/some.plugin.eth"

        val env = mapOf("foo" to "bar")

        val config = ClientConfigBuilder().setEnv(envUri to env).build()

        assertNotNull(config.envs)
        assertEquals(1, config.envs!!.size)
        assertEquals(env, config.envs!![Uri(envUri)])
    }

    @Test
    fun shouldSetAnEnvOverAnExistingEnv() {
        val envUri = "wrap://ens/some.plugin.eth"

        val env1 = mapOf("foo" to "bar")
        val env2 = mapOf("bar" to "baz")

        val config = ClientConfigBuilder()
            .addEnv(envUri to env1)
            .setEnv(envUri to env2)
            .build()

        assertNotNull(config.envs)
        assertEquals(1, config.envs!!.size)
        assertEquals(env2, config.envs!![Uri(envUri)])
    }

    @Test
    fun shouldAddAnInterfaceImplementationForANonExistentInterface() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri = "wrap://ens/interface.impl.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementation(interfaceUri, implUri)
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(Uri(interfaceUri) to listOf(Uri(implUri))),
            config.interfaces
        )
    }

    @Test
    fun shouldAddAnInterfaceImplementationForAnInterfaceThatAlreadyExists() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementation(interfaceUri, implUri1)
            .addInterfaceImplementation(interfaceUri, implUri2)
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(Uri(interfaceUri) to listOf(Uri(implUri1), Uri(implUri2))),
            config.interfaces
        )
    }

    @Test
    fun shouldAddDifferentImplementationsForDifferentInterfaces() {
        val interfaceUri1 = "wrap://ens/some.interface1.eth"
        val interfaceUri2 = "wrap://ens/some.interface2.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"
        val implUri3 = "wrap://ens/interface.impl3.eth"
        val implUri4 = "wrap://ens/interface.impl4.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementation(interfaceUri1, implUri1)
            .addInterfaceImplementation(interfaceUri2, implUri2)
            .addInterfaceImplementation(interfaceUri1, implUri3)
            .addInterfaceImplementation(interfaceUri2, implUri4)
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 2) {
            fail("Expected 2 interfaces, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(
                Uri(interfaceUri1) to listOf(Uri(implUri1), Uri(implUri3)),
                Uri(interfaceUri2) to listOf(Uri(implUri2), Uri(implUri4))
            ),
            config.interfaces
        )
    }

    @Test
    fun shouldAddMultipleImplementationsForANonExistentInterface() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementations(interfaceUri, listOf(implUri1, implUri2))
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(Uri(interfaceUri) to listOf(Uri(implUri1), Uri(implUri2))),
            config.interfaces
        )
    }

    @Test
    fun shouldAddMultipleImplementationsForAnExistingInterface() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"
        val implUri3 = "wrap://ens/interface.impl3.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementations(interfaceUri, listOf(implUri1))
            .addInterfaceImplementations(interfaceUri, listOf(implUri2, implUri3))
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(Uri(interfaceUri) to listOf(Uri(implUri1), Uri(implUri2), Uri(implUri3))),
            config.interfaces
        )
    }

    @Test
    fun shouldAddMultipleDifferentImplementationsForDifferentInterfaces() {
        val interfaceUri1 = "wrap://ens/some.interface1.eth"
        val interfaceUri2 = "wrap://ens/some.interface2.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"
        val implUri3 = "wrap://ens/interface.impl3.eth"
        val implUri4 = "wrap://ens/interface.impl4.eth"
        val implUri5 = "wrap://ens/interface.impl5.eth"
        val implUri6 = "wrap://ens/interface.impl6.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementation(interfaceUri1, implUri1)
            .addInterfaceImplementation(interfaceUri2, implUri2)
            .addInterfaceImplementations(interfaceUri1, listOf(implUri3, implUri5))
            .addInterfaceImplementations(interfaceUri2, listOf(implUri4, implUri6))
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 2) {
            fail("Expected 2 interfaces, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(
                Uri(interfaceUri1) to listOf(Uri(implUri1), Uri(implUri3), Uri(implUri5)),
                Uri(interfaceUri2) to listOf(Uri(implUri2), Uri(implUri4), Uri(implUri6))
            ),
            config.interfaces
        )
    }

    @Test
    fun shouldRemoveAnInterfaceImplementation() {
        val interfaceUri1 = "wrap://ens/some.interface1.eth"
        val interfaceUri2 = "wrap://ens/some.interface2.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementations(interfaceUri1, listOf(implUri1, implUri2))
            .addInterfaceImplementations(interfaceUri2, listOf(implUri1, implUri2))
            .removeInterfaceImplementation(interfaceUri1, implUri2)
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 2) {
            fail("Expected 2 interfaces, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(
                Uri(interfaceUri1) to listOf(Uri(implUri1)),
                Uri(interfaceUri2) to listOf(Uri(implUri1), Uri(implUri2))
            ),
            config.interfaces
        )
    }

    @Test
    fun shouldCompletelyRemoveAnInterfaceIfThereAreNoImplementationsLeft() {
        val interfaceUri1 = "wrap://ens/some.interface1.eth"
        val interfaceUri2 = "wrap://ens/some.interface2.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"

        val config = ClientConfigBuilder()
            .addInterfaceImplementations(interfaceUri1, listOf(implUri1, implUri2))
            .addInterfaceImplementations(interfaceUri2, listOf(implUri1, implUri2))
            .removeInterfaceImplementation(interfaceUri1, implUri1)
            .removeInterfaceImplementation(interfaceUri1, implUri2)
            .build()

        if (config.interfaces == null || config.interfaces!!.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(Uri(interfaceUri2) to listOf(Uri(implUri1), Uri(implUri2))),
            config.interfaces
        )
    }

    @Test
    fun shouldAddAnUriRedirect() {
        val from = "wrap://ens/from.this.ens"
        val to = "wrap://ens/to.that.ens"

        val builder = ClientConfigBuilder().addRedirect(from to to)

        val config = builder.build()
        val builderConfig = builder.config

        assertNotNull(config)
        assertEquals(
            emptyBuilderConfig.copy(redirects = mutableMapOf(from to to)),
            builderConfig
        )
    }

    @Test
    fun shouldAddTwoUriRedirectsWithDifferentFromUris() {
        val from1 = "wrap://ens/from.this1.ens"
        val to1 = "wrap://ens/to.that1.ens"
        val from2 = "wrap://ens/from.this2.ens"
        val to2 = "wrap://ens/to.that2.ens"

        val builder = ClientConfigBuilder()
            .addRedirect(from1 to to1)
            .addRedirect(from2 to to2)

        val config = builder.build()
        val builderConfig = builder.config

        assertNotNull(config)
        assertEquals(
            emptyBuilderConfig.copy(redirects = mutableMapOf(from1 to to1, from2 to to2)),
            builderConfig
        )
    }

    @Test
    fun shouldOverwriteAnExistingUriRedirectIfFromMatchesOnAdd() {
        val from1 = "wrap://ens/from1.this.ens"
        val from2 = "wrap://ens/from2.this.ens"
        val to1 = "wrap://ens/to.that1.ens"
        val to2 = "wrap://ens/to.that2.ens"

        val builder = ClientConfigBuilder()
            .addRedirect(from1 to to1)
            .addRedirect(from2 to to1)
            .addRedirect(from1 to to2)

        val config = builder.build()
        val builderConfig = builder.config

        assertNotNull(config)
        assertEquals(
            emptyBuilderConfig.copy(redirects = mutableMapOf(from1 to to2, from2 to to1)),
            builderConfig
        )
    }

    @Test
    fun shouldRemoveAnUriRedirect() {
        val from1 = "wrap://ens/from.this1.ens"
        val to1 = "wrap://ens/to.that1.ens"
        val from2 = "wrap://ens/from.this2.ens"
        val to2 = "wrap://ens/to.that2.ens"
        val builder = ClientConfigBuilder()
            .addRedirect(from1 to to1)
            .addRedirect(from2 to to2)
            .removeRedirect(from1)

        val config = builder.build()
        val builderConfig = builder.config

        assertNotNull(config)
        assertEquals(
            emptyBuilderConfig.copy(redirects = mutableMapOf(from2 to to2)),
            builderConfig
        )
    }

    @Test
    fun shouldSetUriResolver() {
        val uriResolver = MockUriResolver(
            "wrap://ens/from.eth",
            "wrap://ens/to.eth"
        )

        val builder = ClientConfigBuilder().addResolver(uriResolver)

        val config = builder.build()
        val builderConfig = builder.config

        assertNotNull(config)
        assertEquals(mutableListOf<UriResolver>(uriResolver), builderConfig.resolvers)
    }

    @Test
    fun shouldAddMultipleResolvers() {
        val uriResolver1 = MockUriResolver(
            "wrap://ens/from1.eth",
            "wrap://ens/to1.eth"
        )
        val uriResolver2 = MockUriResolver(
            "wrap://ens/from2.eth",
            "wrap://ens/to2.eth"
        )

        val builder = ClientConfigBuilder()
            .addResolver(uriResolver1)
            .addResolver(uriResolver2)

        val config = builder.build()
        val builderConfig = builder.config

        assertNotNull(config)
        assertEquals(mutableListOf<UriResolver>(uriResolver1, uriResolver2), builderConfig.resolvers)
    }

    @Test
    fun shouldAddAPackage() {
        val uri = "wrap://ens/some.package.eth"

        val builderConfig = ClientConfigBuilder()
            .addPackage(uri to mockWrapPackage)
            .config

        assertEquals(
            mutableMapOf(
                uri to mockWrapPackage
            ),
            builderConfig.packages
        )
    }

    @Test
    fun shouldAddMultiplePackages() {
        val uri1 = "wrap://ens/some1.package.eth"
        val uri2 = "wrap://ens/some2.package.eth"

        val builderConfig = ClientConfigBuilder().addPackages(
            mapOf(
                uri1 to mockWrapPackage,
                uri2 to mockWrapPackage
            )
        ).config

        assertEquals(
            mutableMapOf(
                uri1 to mockWrapPackage,
                uri2 to mockWrapPackage
            ),
            builderConfig.packages
        )
    }

    @Test
    fun shouldRemoveAPackage() {
        val uri1 = "wrap://ens/some1.package.eth"
        val uri2 = "wrap://ens/some2.package.eth"

        val builderConfig = ClientConfigBuilder()
            .addPackages(
                mapOf(
                    uri1 to mockWrapPackage,
                    uri2 to mockWrapPackage
                )
            )
            .removePackage(uri1).config

        assertEquals(
            mutableMapOf(
                uri2 to mockWrapPackage
            ),
            builderConfig.packages
        )
    }

    @Test
    fun shouldAddAWrapper() {
        val uri = "wrap://ens/some.wrapper.eth"

        val builderConfig = ClientConfigBuilder().addWrapper(uri to mockWrapper).config

        assertEquals(
            mutableMapOf(uri to mockWrapper),
            builderConfig.wrappers
        )
    }

    @Test
    fun shouldAddMultipleWrappers() {
        val uri1 = "wrap://ens/some1.wrapper.eth"
        val uri2 = "wrap://ens/some2.wrapper.eth"

        val builderConfig = ClientConfigBuilder().addWrappers(
            mapOf(
                uri1 to mockWrapper,
                uri2 to mockWrapper
            )
        ).config

        assertEquals(
            mapOf(
                uri1 to mockWrapper,
                uri2 to mockWrapper
            ),
            builderConfig.wrappers
        )
    }

    @Test
    fun shouldRemoveAWrapper() {
        val uri1 = "wrap://ens/some1.wrapper.eth"
        val uri2 = "wrap://ens/some2.wrapper.eth"

        val builderConfig = ClientConfigBuilder()
            .addWrappers(
                mapOf(
                    uri1 to mockWrapper,
                    uri2 to mockWrapper
                )
            )
            .removeWrapper(uri1).config

        assertEquals(
            mapOf(uri2 to mockWrapper),
            builderConfig.wrappers
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForEnvs() {
        val shortUri = "ens/some1.wrapper.eth"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builderConfig1 = ClientConfigBuilder()
            .addEnv(shortUri to mapOf("foo" to "bar"))
            .addEnv(longUri to mapOf("bar" to "baz")).config

        assertEquals(
            mutableMapOf(
                Uri(shortUri).uri to mapOf<String, Any>("foo" to "bar"),
                Uri(longUri).uri to mapOf("bar" to "baz")
            ),
            builderConfig1.envs
        )

        val builderConfig2 = ClientConfigBuilder()
            .add(builderConfig1)
            .removeEnv(shortUri).config

        assertEquals(
            mutableMapOf(
                Uri(longUri).uri to mapOf<String, Any>("bar" to "baz")
            ),
            builderConfig2.envs
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForInterfaceImplementations() {
        val shortUri = "ens/some1.wrapper.eth"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builderConfig1 = ClientConfigBuilder()
            .addInterfaceImplementation(shortUri, longUri)
            .addInterfaceImplementation(longUri, shortUri).config

        assertEquals(
            mapOf(
                Uri(shortUri).uri to setOf(Uri(longUri).uri),
                Uri(longUri).uri to setOf(Uri(shortUri).uri)
            ),
            builderConfig1.interfaces
        )

        val builderConfig2 = ClientConfigBuilder()
            .add(builderConfig1)
            .removeInterfaceImplementation(shortUri, longUri).config

        assertEquals(
            mapOf(
                Uri(longUri).uri to setOf(Uri(shortUri).uri)
            ),
            builderConfig2.interfaces
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForRedirects() {
        val shortUri = "ens/some1.wrapper.eth"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builderConfig1 = ClientConfigBuilder()
            .addRedirect(shortUri to longUri)
            .addRedirect(longUri to shortUri).config

        assertEquals(
            mapOf(
                Uri(shortUri).uri to Uri(longUri).uri,
                Uri(longUri).uri to Uri(shortUri).uri
            ),
            builderConfig1.redirects
        )

        val builderConfig2 = ClientConfigBuilder()
            .add(builderConfig1)
            .removeRedirect(shortUri).config

        assertEquals(
            mapOf(
                Uri(longUri).uri to Uri(shortUri).uri
            ),
            builderConfig2.redirects
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForPackages() {
        val shortUri = "ens/some1.package.eth"
        val longUri = "wrap://ens/some2.package.eth"

        val builderConfig1 = ClientConfigBuilder().addPackages(
            mapOf(
                shortUri to mockWrapPackage,
                longUri to mockWrapPackage
            )
        ).config

        assertEquals(
            mapOf(
                Uri(shortUri).uri to mockWrapPackage,
                Uri(longUri).uri to mockWrapPackage
            ),
            builderConfig1.packages
        )

        val builderConfig2 = ClientConfigBuilder()
            .add(builderConfig1)
            .removePackage(shortUri).config

        assertEquals(
            mapOf(
                Uri(longUri).uri to mockWrapPackage
            ),
            builderConfig2.packages
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForWrappers() {
        val shortUri = "ens/some1.wrapper.eth"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builderConfig1 = ClientConfigBuilder().addWrappers(
            mapOf(
                shortUri to mockWrapper,
                longUri to mockWrapper
            )
        ).config

        assertEquals(
            mapOf(
                Uri(shortUri).uri to mockWrapper,
                Uri(longUri).uri to mockWrapper
            ),
            builderConfig1.wrappers
        )

        val builderConfig2 = ClientConfigBuilder()
            .add(builderConfig1)
            .removeWrapper(shortUri).config

        assertEquals(
            mapOf(Uri(longUri).uri to mockWrapper),
            builderConfig2.wrappers
        )
    }
}
