package io.polywrap.configBuilder

import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.WrapEnv
import io.polywrap.core.resolution.UriResolver
import kotlin.collections.Map

/**
 * Provides a base implementation of the [IConfigBuilder] interface.
 */
abstract class BaseConfigBuilder : IConfigBuilder {

    override val config = BuilderConfig(
        envs = mutableMapOf(),
        interfaces = mutableMapOf(),
        redirects = mutableMapOf(),
        wrappers = mutableMapOf(),
        packages = mutableMapOf(),
        resolvers = mutableListOf(),
        ffiBundles = mutableListOf()
    )

    override fun addBundle(bundle: DefaultBundle): IConfigBuilder = this.apply {
        config.ffiBundles.add(bundle)
    }

    override fun addBundle(bundle: Bundle): IConfigBuilder = this.apply {
        bundle.items.forEach { (uri, item) ->
            item.pkg?.let { addPackage(uri to it) }
            item.implements?.forEach { addInterfaceImplementation(it.toString(), uri) }
            item.redirectFrom?.forEach { addRedirect(it.toString() to uri) }
            item.env?.let { addEnv(uri to it) }
        }
    }

    override fun add(config: BuilderConfig): IConfigBuilder = this.apply {
        addEnvs(config.envs)
        config.redirects.forEach { addRedirect(it.toPair()) }
        config.wrappers.forEach { addWrapper(it.toPair()) }
        config.packages.forEach { addPackage(it.toPair()) }
        config.interfaces.forEach { (interfaceUri, implementations) ->
            addInterfaceImplementations(interfaceUri, implementations.toList())
        }
        addResolvers(config.resolvers)
    }

    override fun addWrapper(wrapper: Pair<String, Wrapper>): IConfigBuilder = this.apply {
        config.wrappers[validateUri(wrapper.first)] = wrapper.second
    }

    override fun addWrappers(wrappers: Map<String, Wrapper>): IConfigBuilder = this.apply {
        wrappers.forEach { addWrapper(it.toPair()) }
    }

    override fun removeWrapper(uri: String): IConfigBuilder = this.apply {
        config.wrappers.remove(validateUri(uri))
    }

    override fun addPackage(wrapPackage: Pair<String, WrapPackage>): IConfigBuilder = this.apply {
        config.packages[validateUri(wrapPackage.first)] = wrapPackage.second
    }

    override fun addPackages(packages: Map<String, WrapPackage>): IConfigBuilder = this.apply {
        packages.forEach { addPackage(it.toPair()) }
    }

    override fun removePackage(uri: String): IConfigBuilder = this.apply {
        config.packages.remove(validateUri(uri))
    }

    override fun addEnv(env: Pair<String, WrapEnv>): IConfigBuilder = this.apply {
        val sanitizedUri = validateUri(env.first)
        config.envs[sanitizedUri] = (config.envs[sanitizedUri] ?: emptyMap()) + env.second
    }

    override fun addEnvs(envs: Map<String, WrapEnv>): IConfigBuilder = this.apply {
        envs.forEach { env -> addEnv(env.toPair()) }
    }

    override fun removeEnv(uri: String): IConfigBuilder = this.apply {
        config.envs.remove(validateUri(uri))
    }

    override fun setEnv(env: Pair<String, WrapEnv>): IConfigBuilder = this.apply {
        config.envs[validateUri(env.first)] = env.second
    }

    override fun addInterfaceImplementation(
        interfaceUri: String,
        implementationUri: String
    ): IConfigBuilder = this.apply {
        val sanitizedInterfaceUri = validateUri(interfaceUri)
        val existingInterface = this.config.interfaces[sanitizedInterfaceUri]

        if (existingInterface != null) {
            existingInterface.add(validateUri(implementationUri))
        } else {
            this.config.interfaces[sanitizedInterfaceUri] = mutableSetOf(validateUri(implementationUri))
        }
    }

    override fun addInterfaceImplementations(
        interfaceUri: String,
        implementationUris: List<String>
    ): IConfigBuilder = this.apply {
        val sanitizedInterfaceUri = validateUri(interfaceUri)
        val existingInterface = this.config.interfaces[sanitizedInterfaceUri]

        if (existingInterface != null) {
            implementationUris.forEach { existingInterface.add(validateUri(it)) }
        } else {
            val sanitizedImplUris = implementationUris.map { validateUri(it) }
            this.config.interfaces[sanitizedInterfaceUri] = sanitizedImplUris.toMutableSet()
        }
    }

    override fun removeInterfaceImplementation(
        interfaceUri: String,
        implementationUri: String
    ): IConfigBuilder = this.apply {
        val sanitizedInterfaceUri = validateUri(interfaceUri)
        val existingInterface = this.config.interfaces[sanitizedInterfaceUri] ?: return this

        existingInterface.remove(validateUri(implementationUri))

        if (existingInterface.isEmpty()) {
            this.config.interfaces.remove(sanitizedInterfaceUri)
        }
    }

    override fun addRedirect(redirect: Pair<String, String>): IConfigBuilder = this.apply {
        this.config.redirects[validateUri(redirect.first)] = validateUri(redirect.second)
    }

    override fun addRedirects(redirects: Map<String, String>): IConfigBuilder = this.apply {
        redirects.forEach { addRedirect(it.toPair()) }
    }

    override fun removeRedirect(from: String): IConfigBuilder = this.apply {
        this.config.redirects.remove(validateUri(from))
    }

    override fun addResolver(resolver: UriResolver): IConfigBuilder = this.apply {
        this.config.resolvers.add(resolver)
    }

    override fun addResolvers(resolvers: List<UriResolver>): IConfigBuilder = this.apply {
        resolvers.forEach { addResolver(it) }
    }
}
