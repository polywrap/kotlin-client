package configBuilder

import io.polywrap.configBuilder.BaseConfigBuilder
import io.polywrap.configBuilder.BuilderConfig
import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.configBuilder.DefaultBundle
import io.polywrap.core.Invoker
import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.WrapEnv
import io.polywrap.core.resolution.UriResolver
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriPackageOrWrapper
import uniffi.polywrap_native.FfiUriResolutionContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class ConfigBuilderTest {

    private val emptyBuilderConfig = ConfigBuilder().config

    class MockUriResolver(val from: String, val to: String) : UriResolver {
        override fun tryResolveUri(
            uri: FfiUri,
            invoker: FfiInvoker,
            resolutionContext: FfiUriResolutionContext
        ): FfiUriPackageOrWrapper {
            throw NotImplementedError()
        }

        override fun close() {}
    }

    private val mockWrapPackage: WrapPackage = object : WrapPackage {
        override fun createWrapper() = throw NotImplementedError()
        override fun getManifest() = throw NotImplementedError()
        override fun getFile(path: String): Result<ByteArray> {
            throw NotImplementedError()
        }
    }

    private val mockWrapper: Wrapper = object : Wrapper {
        override fun invoke(
            method: String,
            args: List<UByte>?,
            env: List<UByte>?,
            invoker: FfiInvoker
        ): List<UByte> {
            throw NotImplementedError()
        }

        override fun invoke(
            method: String,
            args: ByteArray?,
            env: ByteArray?,
            invoker: Invoker
        ): Result<ByteArray> {
            throw NotImplementedError()
        }
    }

    private val testEnv1: Pair<String, WrapEnv> = Pair(
        "wrap://ens/test.plugin.one",
        mapOf("test" to "value")
    )

    private val testEnv2: Pair<String, WrapEnv> = Pair(
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
        ConfigBuilder().build()
    }

    @Test
    fun shouldSuccessfullyAddConfigObjectAndBuild() {
        val configObject = BuilderConfig(
            envs = testEnvs,
            interfaces = testInterfaces,
            redirects = testUriRedirects,
            wrappers = mutableMapOf(),
            packages = mutableMapOf(),
            resolvers = mutableListOf(testUriResolver),
            ffiBundles = mutableListOf()
        )

        val builder = ConfigBuilder().add(configObject)
        val builderConfig = builder.config
        assertEquals(configObject, builderConfig)
    }

    @Test
    fun shouldSuccessfullyAddTheDefaultConfig() {
        val builder = ConfigBuilder().addDefaults()

        val clientConfig = builder.build()
        val builderConfig = builder.config

        assertNotNull(clientConfig)

        val expectedBuilderConfig = BuilderConfig(
            envs = mutableMapOf(),
            interfaces = mutableMapOf(),
            redirects = mutableMapOf(),
            wrappers = mutableMapOf(),
            packages = mutableMapOf(),
            resolvers = mutableListOf(),
            ffiBundles = mutableListOf(DefaultBundle.System, DefaultBundle.Web3)
        )
        assertEquals(expectedBuilderConfig, builderConfig)
    }

    @Test
    fun shouldSuccessfullyAddAnEnv() {
        val envUri = "wrap://ens/some-plugin.polywrap.eth"
        val env = mapOf(
            "foo" to "bar",
            "baz" to mapOf("biz" to "buz")
        )

        val config = (ConfigBuilder().addEnv(envUri to env) as BaseConfigBuilder).config

        assertNotNull(config.envs)
        assertEquals(1, config.envs.size)
        assertEquals(env, config.envs[envUri])
    }

    @Test
    fun shouldSuccessfullyAddToAnExistingEnv() {
        val envUri = "wrap://ens/some-plugin.polywrap.eth"
        val env1 = mapOf("foo" to "bar")
        val env2 = mapOf("baz" to mapOf("biz" to "buz"))

        val builder = ConfigBuilder()
            .addEnv(envUri to env1)
            .addEnv(envUri to env2)
        val config = builder.config

        val expectedEnv = env1 + env2

        assertNotNull(config.envs)
        assertEquals(1, config.envs.size)
        assertEquals(expectedEnv, config.envs[envUri])
    }

    @Test
    fun shouldSuccessfullyAddTwoSeparateEnvs() {
        val builder = ConfigBuilder()
            .addEnv(testEnvs.keys.first() to testEnvs.values.first())
            .addEnv(testEnvs.keys.last() to testEnvs.values.last())
        val config = builder.config

        assertNotNull(config.envs)
        assertEquals(2, config.envs.size)
        assertEquals(testEnvs.values.first(), config.envs[testEnvs.keys.first()])
        assertEquals(testEnvs.values.last(), config.envs[testEnvs.keys.last()])
    }

    @Test
    fun shouldRemoveAnEnv() {
        val builder = ConfigBuilder()
            .addEnv(testEnvs.keys.first() to testEnvs.values.first())
            .addEnv(testEnvs.keys.last() to testEnvs.values.last())
            .removeEnv(testEnvs.keys.first())
        val config = builder.config

        assertNotNull(config.envs)
        assertEquals(1, config.envs.size)
        assertEquals(testEnvs.values.last(), config.envs[testEnvs.keys.last()])
    }

    @Test
    fun shouldSetAnEnv() {
        val envUri = "wrap://ens/some.plugin.eth"

        val env = mapOf("foo" to "bar")

        val builder = ConfigBuilder().setEnv(envUri to env)
        val config = builder.config

        assertNotNull(config.envs)
        assertEquals(1, config.envs.size)
        assertEquals(env, config.envs[envUri])
    }

    @Test
    fun shouldSetAnEnvOverAnExistingEnv() {
        val envUri = "wrap://ens/some.plugin.eth"

        val env1 = mapOf("foo" to "bar")
        val env2 = mapOf("bar" to "baz")

        val builder = ConfigBuilder()
            .addEnv(envUri to env1)
            .setEnv(envUri to env2)
        val config = builder.config

        assertNotNull(config.envs)
        assertEquals(1, config.envs.size)
        assertEquals(env2, config.envs[envUri])
    }

    @Test
    fun shouldAddAnInterfaceImplementationForANonExistentInterface() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri = "wrap://ens/interface.impl.eth"

        val builder = ConfigBuilder()
            .addInterfaceImplementation(interfaceUri, implUri)
        val config = builder.config

        if (config.interfaces.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(interfaceUri to mutableSetOf(implUri)),
            config.interfaces
        )
    }

    @Test
    fun shouldAddAnInterfaceImplementationForAnInterfaceThatAlreadyExists() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"

        val builder = ConfigBuilder()
            .addInterfaceImplementation(interfaceUri, implUri1)
            .addInterfaceImplementation(interfaceUri, implUri2)
        val config = builder.config

        if (config.interfaces.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(interfaceUri to mutableSetOf(implUri1, implUri2)),
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

        val builder = ConfigBuilder()
            .addInterfaceImplementation(interfaceUri1, implUri1)
            .addInterfaceImplementation(interfaceUri2, implUri2)
            .addInterfaceImplementation(interfaceUri1, implUri3)
            .addInterfaceImplementation(interfaceUri2, implUri4)
        val config = builder.config

        if (config.interfaces.size != 2) {
            fail("Expected 2 interfaces, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(
                interfaceUri1 to mutableSetOf(implUri1, implUri3),
                interfaceUri2 to mutableSetOf(implUri2, implUri4)
            ),
            config.interfaces
        )
    }

    @Test
    fun shouldAddMultipleImplementationsForANonExistentInterface() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"

        val builder = ConfigBuilder()
            .addInterfaceImplementations(interfaceUri, listOf(implUri1, implUri2))
        val config = builder.config

        if (config.interfaces.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(interfaceUri to mutableSetOf(implUri1, implUri2)),
            config.interfaces
        )
    }

    @Test
    fun shouldAddMultipleImplementationsForAnExistingInterface() {
        val interfaceUri = "wrap://ens/some.interface.eth"
        val implUri1 = "wrap://ens/interface.impl1.eth"
        val implUri2 = "wrap://ens/interface.impl2.eth"
        val implUri3 = "wrap://ens/interface.impl3.eth"

        val builder = ConfigBuilder()
            .addInterfaceImplementations(interfaceUri, listOf(implUri1))
            .addInterfaceImplementations(interfaceUri, listOf(implUri2, implUri3))
        val config = builder.config

        if (config.interfaces.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(interfaceUri to mutableSetOf(implUri1, implUri2, implUri3)),
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

        val builder = ConfigBuilder()
            .addInterfaceImplementation(interfaceUri1, implUri1)
            .addInterfaceImplementation(interfaceUri2, implUri2)
            .addInterfaceImplementations(interfaceUri1, listOf(implUri3, implUri5))
            .addInterfaceImplementations(interfaceUri2, listOf(implUri4, implUri6))
        val config = builder.config

        if (config.interfaces.size != 2) {
            fail("Expected 2 interfaces, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(
                interfaceUri1 to mutableSetOf(implUri1, implUri3, implUri5),
                interfaceUri2 to mutableSetOf(implUri2, implUri4, implUri6)
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

        val builder = ConfigBuilder()
            .addInterfaceImplementations(interfaceUri1, listOf(implUri1, implUri2))
            .addInterfaceImplementations(interfaceUri2, listOf(implUri1, implUri2))
            .removeInterfaceImplementation(interfaceUri1, implUri2)
        val config = builder.config

        if (config.interfaces.size != 2) {
            fail("Expected 2 interfaces, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(
                interfaceUri1 to mutableSetOf(implUri1),
                interfaceUri2 to mutableSetOf(implUri1, implUri2)
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

        val builder = ConfigBuilder()
            .addInterfaceImplementations(interfaceUri1, listOf(implUri1, implUri2))
            .addInterfaceImplementations(interfaceUri2, listOf(implUri1, implUri2))
            .removeInterfaceImplementation(interfaceUri1, implUri1)
            .removeInterfaceImplementation(interfaceUri1, implUri2)
        val config = builder.config

        if (config.interfaces.size != 1) {
            fail("Expected 1 interface, received: ${config.interfaces}")
        }

        assertEquals(
            mapOf(interfaceUri2 to mutableSetOf(implUri1, implUri2)),
            config.interfaces
        )
    }

    @Test
    fun shouldAddAnUriRedirect() {
        val from = "wrap://ens/from.this.ens"
        val to = "wrap://ens/to.that.ens"

        val builder = ConfigBuilder().addRedirect(from to to)

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

        val builder = ConfigBuilder()
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

        val builder = ConfigBuilder()
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
        val builder = ConfigBuilder()
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

        val builder = ConfigBuilder().addResolver(uriResolver)

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

        val builder = ConfigBuilder()
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

        val builder = ConfigBuilder()
            .addPackage(uri to mockWrapPackage)
        val config = builder.config

        assertEquals(
            mutableMapOf(
                uri to mockWrapPackage
            ),
            config.packages
        )
    }

    @Test
    fun shouldAddMultiplePackages() {
        val uri1 = "wrap://ens/some1.package.eth"
        val uri2 = "wrap://ens/some2.package.eth"

        val builder = ConfigBuilder().addPackages(
            mapOf(
                uri1 to mockWrapPackage,
                uri2 to mockWrapPackage
            )
        )
        val config = builder.config

        assertEquals(
            mutableMapOf(
                uri1 to mockWrapPackage,
                uri2 to mockWrapPackage
            ),
            config.packages
        )
    }

    @Test
    fun shouldRemoveAPackage() {
        val uri1 = "wrap://ens/some1.package.eth"
        val uri2 = "wrap://ens/some2.package.eth"

        val builder = ConfigBuilder()
            .addPackages(
                mapOf(
                    uri1 to mockWrapPackage,
                    uri2 to mockWrapPackage
                )
            )
            .removePackage(uri1)
        val config = builder.config

        assertEquals(
            mutableMapOf(
                uri2 to mockWrapPackage
            ),
            config.packages
        )
    }

    @Test
    fun shouldAddAWrapper() {
        val uri = "wrap://ens/some.wrapper.eth"

        val builder = ConfigBuilder().addWrapper(uri to mockWrapper)
        val config = builder.config

        assertEquals(
            mutableMapOf(uri to mockWrapper),
            config.wrappers
        )
    }

    @Test
    fun shouldAddMultipleWrappers() {
        val uri1 = "wrap://ens/some1.wrapper.eth"
        val uri2 = "wrap://ens/some2.wrapper.eth"

        val builder = ConfigBuilder().addWrappers(
            mapOf(
                uri1 to mockWrapper,
                uri2 to mockWrapper
            )
        )
        val config = builder.config

        assertEquals(
            mapOf(
                uri1 to mockWrapper,
                uri2 to mockWrapper
            ),
            config.wrappers
        )
    }

    @Test
    fun shouldRemoveAWrapper() {
        val uri1 = "wrap://ens/some1.wrapper.eth"
        val uri2 = "wrap://ens/some2.wrapper.eth"

        val builder = ConfigBuilder()
            .addWrappers(
                mapOf(
                    uri1 to mockWrapper,
                    uri2 to mockWrapper
                )
            )
            .removeWrapper(uri1)
        val config = builder.config

        assertEquals(
            mapOf(uri2 to mockWrapper),
            config.wrappers
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForEnvs() {
        val shortUri = "ens/some1.wrapper.eth"
        val shortUriSanitized = "wrap://$shortUri"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builder1 = ConfigBuilder()
            .addEnv(shortUri to mapOf("foo" to "bar"))
            .addEnv(longUri to mapOf("bar" to "baz"))
        val config1 = builder1.config

        assertEquals(
            mutableMapOf(
                shortUriSanitized to mapOf<String, Any>("foo" to "bar"),
                longUri to mapOf("bar" to "baz")
            ),
            config1.envs
        )

        val builder2 = ConfigBuilder()
            .add(config1)
            .removeEnv(shortUri)
        val config2 = builder2.config

        assertEquals(
            mutableMapOf(
                longUri to mapOf<String, Any>("bar" to "baz")
            ),
            config2.envs
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForInterfaceImplementations() {
        val shortUri = "ens/some1.wrapper.eth"
        val shortUriSanitized = "wrap://$shortUri"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builder1 = ConfigBuilder()
            .addInterfaceImplementation(shortUri, longUri)
            .addInterfaceImplementation(longUri, shortUri)
        val config1 = builder1.config

        assertEquals(
            mapOf(
                shortUriSanitized to mutableSetOf(longUri),
                longUri to mutableSetOf(shortUriSanitized)
            ),
            config1.interfaces
        )

        val builder2 = ConfigBuilder()
            .add(config1)
            .removeInterfaceImplementation(shortUri, longUri)
        val config2 = builder2.config

        assertEquals(
            mapOf(longUri to setOf(shortUriSanitized)),
            config2.interfaces
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForRedirects() {
        val shortUri = "ens/some1.wrapper.eth"
        val shortUriSanitized = "wrap://$shortUri"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builder1 = ConfigBuilder()
            .addRedirect(shortUri to longUri)
            .addRedirect(longUri to shortUri)
        val config1 = builder1.config

        assertEquals(
            mapOf(
                shortUriSanitized to longUri,
                longUri to shortUriSanitized
            ),
            config1.redirects
        )

        val builder2 = ConfigBuilder()
            .add(config1)
            .removeRedirect(shortUri)
        val config2 = builder2.config

        assertEquals(
            mapOf(longUri to shortUriSanitized),
            config2.redirects
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForPackages() {
        val shortUri = "ens/some1.package.eth"
        val shortUriSanitized = "wrap://$shortUri"
        val longUri = "wrap://ens/some2.package.eth"

        val builder1 = ConfigBuilder().addPackages(
            mapOf(
                shortUri to mockWrapPackage,
                longUri to mockWrapPackage
            )
        )
        val config1 = builder1.config

        assertEquals(
            mapOf(
                shortUriSanitized to mockWrapPackage,
                longUri to mockWrapPackage
            ),
            config1.packages
        )

        val builder2 = ConfigBuilder()
            .add(config1)
            .removePackage(shortUri)
        val config2 = builder2.config

        assertEquals(
            mapOf(longUri to mockWrapPackage),
            config2.packages
        )
    }

    @Test
    fun shouldSanitizeIncomingUrisForWrappers() {
        val shortUri = "ens/some1.wrapper.eth"
        val shortUriSanitized = "wrap://$shortUri"
        val longUri = "wrap://ens/some2.wrapper.eth"

        val builder1 = ConfigBuilder().addWrappers(
            mapOf(
                shortUri to mockWrapper,
                longUri to mockWrapper
            )
        )
        val config1 = builder1.config

        assertEquals(
            mapOf(
                shortUriSanitized to mockWrapper,
                longUri to mockWrapper
            ),
            config1.wrappers
        )

        val builder2 = ConfigBuilder()
            .add(config1)
            .removeWrapper(shortUri)
        val config2 = builder2.config

        assertEquals(
            mapOf(longUri to mockWrapper),
            config2.wrappers
        )
    }
}
