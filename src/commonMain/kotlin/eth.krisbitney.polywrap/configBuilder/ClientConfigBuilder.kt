package eth.krisbitney.polywrap.configBuilder

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.ClientConfig
import eth.krisbitney.polywrap.core.types.WrapperEnv
import eth.krisbitney.polywrap.uriResolvers.SequentialResolver
import eth.krisbitney.polywrap.uriResolvers.RecursiveResolver
import eth.krisbitney.polywrap.uriResolvers.cache.BasicWrapperCache
import eth.krisbitney.polywrap.uriResolvers.cache.SynchronizedCacheResolver
import eth.krisbitney.polywrap.uriResolvers.cache.WrapperCache
import eth.krisbitney.polywrap.uriResolvers.embedded.PackageRedirect
import eth.krisbitney.polywrap.uriResolvers.embedded.StaticResolver
import eth.krisbitney.polywrap.uriResolvers.embedded.UriRedirect
import eth.krisbitney.polywrap.uriResolvers.embedded.WrapperRedirect
import eth.krisbitney.polywrap.uriResolvers.extendable.ExtendableUriResolver

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
