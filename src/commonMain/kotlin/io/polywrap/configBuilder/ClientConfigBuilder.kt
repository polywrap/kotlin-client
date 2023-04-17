package io.polywrap.configBuilder

import io.polywrap.core.resolution.*
import io.polywrap.core.types.ClientConfig
import io.polywrap.core.types.WrapperEnv
import io.polywrap.uriResolvers.RecursiveResolver
import io.polywrap.uriResolvers.SequentialResolver
import io.polywrap.uriResolvers.cache.BasicWrapperCache
import io.polywrap.uriResolvers.cache.CacheResolver
import io.polywrap.uriResolvers.cache.WrapperCache
import io.polywrap.uriResolvers.embedded.PackageRedirect
import io.polywrap.uriResolvers.embedded.StaticResolver
import io.polywrap.uriResolvers.embedded.UriRedirect
import io.polywrap.uriResolvers.embedded.WrapperRedirect
import io.polywrap.uriResolvers.extendable.ExtendableUriResolver

/**
 * A concrete implementation of the [BaseClientConfigBuilder] class.
 * This class builds [ClientConfig] instances using provided configurations.
 */
class ClientConfigBuilder : BaseClientConfigBuilder() {

    /**
     * Adds the default configuration bundle to the current configuration.
     *
     * @return This [ClientConfigBuilder] instance for chaining calls.
     */
    override fun addDefaults(): IClientConfigBuilder {
        return add(DefaultBundle.getConfig())
    }

    /**
     * Builds a [ClientConfig] instance using the current configuration and an optional [WrapperCache].
     *
     * @param cache An optional [WrapperCache] to be used by the [ClientConfig] instance.
     * @return A [ClientConfig] instance based on the current configuration.
     */
    override fun build(cache: WrapperCache?): ClientConfig {
        val static = StaticResolver.from(
            buildRedirects() + buildWrappers() + buildPackages()
        )
        return ClientConfig(
            envs = buildEnvs(),
            interfaces = buildInterfaces(),
            resolver = RecursiveResolver(
                CacheResolver(
                    SequentialResolver(
                        listOf(static) + config.resolvers + listOf(ExtendableUriResolver())
                    ),
                    cache ?: BasicWrapperCache()
                )
            )
        )
    }

    /**
     * Builds a [ClientConfig] instance using the current configuration and a custom [UriResolver].
     *
     * @param resolver A custom [UriResolver] to be used by the [ClientConfig] instance.
     * @return A [ClientConfig] instance based on the current configuration.
     */
    override fun build(resolver: UriResolver): ClientConfig {
        return ClientConfig(
            envs = buildEnvs(),
            interfaces = buildInterfaces(),
            resolver = resolver
        )
    }

    private fun buildEnvs(): Map<Uri, WrapperEnv> {
        return config.envs.mapKeys { Uri(it.key) }
    }

    private fun buildInterfaces(): Map<Uri, List<Uri>> {
        val interfaces = mutableMapOf<Uri, List<Uri>>()

        for ((uri, impls) in config.interfaces) {
            val uriImpls = impls.map { Uri(it) }
            interfaces[Uri(uri)] = uriImpls
        }

        return interfaces
    }

    private fun buildRedirects(): List<UriRedirect> {
        return config.redirects.map { (uri, redirect) ->
            Uri(uri) to Uri(redirect)
        }
    }

    private fun buildWrappers(): List<WrapperRedirect> {
        return config.wrappers.map { (uri, wrapper) ->
            Uri(uri) to wrapper
        }
    }

    private fun buildPackages(): List<PackageRedirect> {
        return config.packages.map { (uri, wrapPackage) ->
            Uri(uri) to wrapPackage
        }
    }
}
