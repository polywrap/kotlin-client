package io.polywrap.configBuilder

import io.polywrap.core.resolution.*
import io.polywrap.core.types.ClientConfig
import io.polywrap.core.types.WrapperEnv
import io.polywrap.uriResolvers.RecursiveResolver
import io.polywrap.uriResolvers.SequentialResolver
import io.polywrap.uriResolvers.cache.BasicWrapperCache
import io.polywrap.uriResolvers.cache.SynchronizedCacheResolver
import io.polywrap.uriResolvers.cache.WrapperCache
import io.polywrap.uriResolvers.embedded.PackageRedirect
import io.polywrap.uriResolvers.embedded.StaticResolver
import io.polywrap.uriResolvers.embedded.UriRedirect
import io.polywrap.uriResolvers.embedded.WrapperRedirect
import io.polywrap.uriResolvers.extendable.ExtendableUriResolver

class ClientConfigBuilder : BaseClientConfigBuilder() {

    override fun addDefaults(): IClientConfigBuilder {
        return add(DefaultBundle.getConfig())
    }

    override fun build(cache: WrapperCache?): ClientConfig {
        val static = StaticResolver.from(
            buildRedirects() + buildWrappers() + buildPackages()
        )
        return ClientConfig(
            envs = buildEnvs(),
            interfaces = buildInterfaces(),
            resolver = RecursiveResolver(
                SynchronizedCacheResolver(
                    SequentialResolver(
                        listOf(static) + config.resolvers + listOf(ExtendableUriResolver())
                    ),
                    cache ?: BasicWrapperCache()
                )
            )
        )
    }

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
