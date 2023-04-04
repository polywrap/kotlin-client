package io.polywrap.configBuilder

import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolver
import io.polywrap.core.types.ClientConfig
import io.polywrap.core.types.WrapPackage
import io.polywrap.core.types.Wrapper
import io.polywrap.core.types.WrapperEnv
import io.polywrap.uriResolvers.cache.WrapperCache
import kotlin.collections.Map

/**
 * Provides a base implementation of the [IClientConfigBuilder] interface.
 */
abstract class BaseClientConfigBuilder : IClientConfigBuilder {

    /**
     * Holds the current configuration being built.
     */
    override val config = BuilderConfig(
        envs = mutableMapOf(),
        interfaces = mutableMapOf(),
        redirects = mutableMapOf(),
        wrappers = mutableMapOf(),
        packages = mutableMapOf(),
        resolvers = mutableListOf()
    )

    /**
     * Adds default configuration bundle to the current configuration.
     *
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    abstract override fun addDefaults(): IClientConfigBuilder

    /**
     * Builds a [ClientConfig] instance with the current builder configuration and an optional [WrapperCache].
     *
     * @param cache Optional [WrapperCache] to be used with the created [ClientConfig] instance.
     * @return A [ClientConfig] instance with the specified configuration.
     */
    abstract override fun build(cache: WrapperCache?): ClientConfig

    /**
     * Builds a [ClientConfig] instance with the current builder configuration and a required [UriResolver].
     *
     * @param resolver Required [UriResolver] to be used with the created [ClientConfig] instance.
     * @return A [ClientConfig] instance with the specified configuration.
     */
    abstract override fun build(resolver: UriResolver): ClientConfig

    /**
     * Adds a [BuilderConfig] instance to the current configuration by merging its properties.
     *
     * @param config The [BuilderConfig] instance to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun add(config: BuilderConfig): IClientConfigBuilder = this.apply {
        addEnvs(config.envs)
        config.redirects.forEach { addRedirect(it.toPair()) }
        config.wrappers.forEach { addWrapper(it.toPair()) }
        config.packages.forEach { addPackage(it.toPair()) }
        config.interfaces.forEach { (interfaceUri, implementations) ->
            addInterfaceImplementations(interfaceUri, implementations.toList())
        }
        addResolvers(config.resolvers)
    }

    /**
     * Adds a wrapper with a specified URI key to the current configuration.
     *
     * @param wrapper A [Pair] of the URI key and the [Wrapper] to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addWrapper(wrapper: Pair<String, Wrapper>): IClientConfigBuilder = this.apply {
        config.wrappers[Uri(wrapper.first).uri] = wrapper.second
    }

    /**
     * Adds a set of wrappers with specified URI keys to the current configuration.
     *
     * @param wrappers A [Map] of URI keys to [Wrapper] instances to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addWrappers(wrappers: Map<String, Wrapper>): IClientConfigBuilder = this.apply {
        wrappers.forEach { addWrapper(it.toPair()) }
    }

    /**
     * Removes a wrapper with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the wrapper to remove.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun removeWrapper(uri: String): IClientConfigBuilder = this.apply {
        config.wrappers.remove(Uri(uri).uri)
    }

    /**
     * Adds a package with a specified URI key to the current configuration.
     *
     * @param wrapPackage A [Pair] of the URI key and the [WrapPackage] to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addPackage(wrapPackage: Pair<String, WrapPackage>): IClientConfigBuilder = this.apply {
        config.packages[Uri(wrapPackage.first).uri] = wrapPackage.second
    }

    /**
     * Adds a set of packages with specified URI keys to the current configuration.
     *
     * @param packages A [Map] of URI keys to [WrapPackage] instances to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addPackages(packages: Map<String, WrapPackage>): IClientConfigBuilder = this.apply {
        packages.forEach { addPackage(it.toPair()) }
    }

    /**
     * Removes a package with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the package to remove.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun removePackage(uri: String): IClientConfigBuilder = this.apply {
        config.packages.remove(Uri(uri).uri)
    }

    /**
     * Adds an environment variable with a specified URI key to the current configuration.
     * If the environment variable already exists, it merges the new values with the existing ones.
     *
     * @param env A [Pair] of the URI key and the [WrapperEnv] to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addEnv(env: Pair<String, WrapperEnv>): IClientConfigBuilder = this.apply {
        val sanitizedUri = Uri(env.first).uri
        config.envs[sanitizedUri] = (config.envs[sanitizedUri] ?: emptyMap()) + env.second
    }

    /**
     * Adds a set of environment variables with specified URI keys to the current configuration.
     *
     * @param envs A [Map] of URI keys to [WrapperEnv] instances to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addEnvs(envs: Map<String, WrapperEnv>): IClientConfigBuilder = this.apply {
        envs.forEach { env -> addEnv(env.toPair()) }
    }

    /**
     * Removes an environment variable with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the environment variable to remove.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun removeEnv(uri: String): IClientConfigBuilder = this.apply {
        config.envs.remove(Uri(uri).uri)
    }

    /**
     * Sets or replaces an environment variable with a specified URI key in the current configuration.
     *
     * @param env A [Pair] of the URI key and the [WrapperEnv] to set.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun setEnv(env: Pair<String, WrapperEnv>): IClientConfigBuilder = this.apply {
        config.envs[Uri(env.first).uri] = env.second
    }

    /**
     * Adds an interface implementation with the specified interface and implementation URIs.
     * If the interface already exists, it adds the new implementation to the existing ones.
     *
     * @param interfaceUri The URI of the interface to associate with the implementation.
     * @param implementationUri The URI of the implementation.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addInterfaceImplementation(
        interfaceUri: String,
        implementationUri: String
    ): IClientConfigBuilder = this.apply {
        val sanitizedInterfaceUri = Uri(interfaceUri).uri
        val existingInterface = this.config.interfaces[sanitizedInterfaceUri]

        if (existingInterface != null) {
            existingInterface.add(Uri(implementationUri).uri)
        } else {
            this.config.interfaces[sanitizedInterfaceUri] = mutableSetOf(Uri(implementationUri).uri)
        }
    }

    /**
     * Adds multiple interface implementations with the specified interface URI and a list of implementation URIs.
     *
     * @param interfaceUri The URI of the interface to associate with the implementations.
     * @param implementationUris A [List] of URIs of the implementations.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addInterfaceImplementations(
        interfaceUri: String,
        implementationUris: List<String>
    ): IClientConfigBuilder = this.apply {
        val sanitizedInterfaceUri = Uri(interfaceUri).uri
        val existingInterface = this.config.interfaces[sanitizedInterfaceUri]

        if (existingInterface != null) {
            implementationUris.forEach { existingInterface.add(Uri(it).uri) }
        } else {
            val sanitizedImplUris = implementationUris.map { Uri(it).uri }
            this.config.interfaces[sanitizedInterfaceUri] = sanitizedImplUris.toMutableSet()
        }
    }

    /**
     * Removes an interface implementation with the specified interface and implementation URIs.
     * If the interface has no remaining implementations, it also removes the interface.
     *
     * @param interfaceUri The URI of the interface associated with the implementation.
     * @param implementationUri The URI of the implementation to remove.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun removeInterfaceImplementation(
        interfaceUri: String,
        implementationUri: String
    ): IClientConfigBuilder = this.apply {
        val sanitizedInterfaceUri = Uri(interfaceUri).uri
        val existingInterface = this.config.interfaces[sanitizedInterfaceUri] ?: return this

        existingInterface.remove(Uri(implementationUri).uri)

        if (existingInterface.isEmpty()) {
            this.config.interfaces.remove(sanitizedInterfaceUri)
        }
    }

    /**
     * Adds a redirect with a specified source and destination URI.
     *
     * @param redirect A [Pair] of the source URI and the destination URI.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addRedirect(redirect: Pair<String, String>): IClientConfigBuilder = this.apply {
        this.config.redirects[Uri(redirect.first).uri] = Uri(redirect.second).uri
    }

    /**
     * Adds multiple redirects to the current configuration.
     *
     * @param redirects A [Map] of source URIs to destination URIs.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addRedirects(redirects: Map<String, String>): IClientConfigBuilder = this.apply {
        redirects.forEach { addRedirect(it.toPair()) }
    }

    /**
     * Removes a redirect with the specified source URI from the current configuration.
     *
     * @param from The source URI of the redirect to remove.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun removeRedirect(from: String): IClientConfigBuilder = this.apply {
        this.config.redirects.remove(Uri(from).uri)
    }

    /**
     * Adds a [UriResolver] to the current configuration.
     *
     * @param resolver The [UriResolver] instance to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addResolver(resolver: UriResolver): IClientConfigBuilder = this.apply {
        this.config.resolvers.add(resolver)
    }

    /**
     * Adds multiple [UriResolver] instances to the current configuration.
     *
     * @param resolvers A [List] of [UriResolver] instances to add.
     * @return This [BaseClientConfigBuilder] instance for chaining calls.
     */
    override fun addResolvers(resolvers: List<UriResolver>): IClientConfigBuilder = this.apply {
        resolvers.forEach { addResolver(it) }
    }
}
