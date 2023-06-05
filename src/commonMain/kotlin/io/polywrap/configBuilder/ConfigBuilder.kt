package io.polywrap.configBuilder

import io.polywrap.client.PolywrapClient
import io.polywrap.core.resolution.*
import io.polywrap.uriResolvers.RecursiveResolver
import io.polywrap.uriResolvers.SequentialResolver
import io.polywrap.uriResolvers.cache.BasicWrapperCache
import io.polywrap.uriResolvers.cache.CacheResolver
import io.polywrap.uriResolvers.cache.WrapperCache
import io.polywrap.uriResolvers.embedded.StaticResolver
import io.polywrap.uriResolvers.extendable.ExtendableUriResolver

/**
 * A concrete implementation of the [BaseClientConfigBuilder] class.
 * This class builds [ClientConfig] instances using provided configurations.
 */
class ConfigBuilder : BaseConfigBuilder() {

    override fun addDefaults(): IConfigBuilder {
        return add(DefaultBundle.getConfig())
    }

    override fun build(cache: WrapperCache, configure: (IConfigBuilder.() -> Unit)?): PolywrapClient {
        configure?.let { this.apply(it) }
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
                    cache
                )
            )
        )
    }

    // TODO: add cache?
    override fun build(configure: (IConfigBuilder.() -> Unit)?): PolywrapClient {
        configure?.let { this.apply(it) }
        val ffiConfigBuilder = FfiConfigBuilder()
        buildEnvs(ffiConfigBuilder)
        buildInterfaces(ffiConfigBuilder)
        buildRedirects(ffiConfigBuilder)
        buildWrappers(ffiConfigBuilder)
        buildPackages(ffiConfigBuilder)
        return PolywrapClient(ffiConfigBuilder.build())
    }

    private fun buildEnvs(ffiConfigBuilder: FfiConfigBuilder) {
        config.envs.forEach { (key, value) ->
            ffiConfigBuilder.addEnv(key, value)
        }
    }

    private fun buildInterfaces(ffiConfigBuilder: FfiConfigBuilder) {
        config.interfaces.forEach { (key, value) ->
            value.forEach {
                ffiConfigBuilder.addInterfaceImplementation(key, it)
            }
        }
    }

    private fun buildRedirects(ffiConfigBuilder: FfiConfigBuilder) {
        config.redirects.forEach { (key, value) ->
            ffiConfigBuilder.addRedirect(key, value)
        }
    }

    private fun buildWrappers(ffiConfigBuilder: FfiConfigBuilder) {
        config.wrappers.forEach { (key, value) ->
            ffiConfigBuilder.addWrapper(key, value)
        }
    }

    private fun buildPackages(ffiConfigBuilder: FfiConfigBuilder) {
        config.packages.forEach { (key, value) ->
            ffiConfigBuilder.addPackage(key, value)
        }
    }
}
