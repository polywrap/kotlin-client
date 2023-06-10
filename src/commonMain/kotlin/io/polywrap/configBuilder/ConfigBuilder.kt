package io.polywrap.configBuilder

import io.polywrap.client.PolywrapClient

/**
 * A concrete implementation of the [BaseConfigBuilder] class.
 * This class builds [PolywrapClient] instances using provided configurations.
 */
class ConfigBuilder : BaseConfigBuilder() {

    override fun addDefaults(): IConfigBuilder {
        return add(DefaultBundle.getConfig())
    }

    override fun build(configure: (IConfigBuilder.() -> Unit)?): PolywrapClient {
        configure?.let { this.apply(it) }
        return FfiConfigBuilder().use {
            buildEnvs(it)
            buildInterfaces(it)
            buildRedirects(it)
            buildWrappers(it)
            buildPackages(it)
            PolywrapClient(it.build())
        }
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
