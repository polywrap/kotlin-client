package io.polywrap.configBuilder

import io.polywrap.client.PolywrapClient
import io.polywrap.core.*
import io.polywrap.core.resolution.UriResolver
import kotlin.collections.List
import kotlin.collections.Map

/**
 * Interface defining a builder for creating configured [PolywrapClient] instances with support for
 * adding, removing, and modifying various configuration options.
 */
interface IConfigBuilder {

    /**
     * Holds the current configuration being built.
     */
    val config: BuilderConfig

    /**
     * Builds a [PolywrapClient] instance with the current builder configuration and
     * optional additional configuration specified through a builder DSL.
     *
     * @param configure Additional configuration to be applied to the builder.
     * @return A [PolywrapClient] instance with the specified configuration.
     */
    fun build(configure: (IConfigBuilder.() -> Unit)? = null): PolywrapClient

    /**
     * Adds the given [BuilderConfig] to the current configuration.
     *
     * @param config The [BuilderConfig] to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun add(config: BuilderConfig): IConfigBuilder

    /**
     * Adds default configuration bundle to the current configuration.
     *
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addDefaults(): IConfigBuilder

    /**
     * Adds a wrapper with a specified URI key to the current configuration.
     *
     * @param wrapper A [Pair] of the URI key and the [Wrapper] to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addWrapper(wrapper: Pair<String, Wrapper>): IConfigBuilder

    /**
     * Adds a set of wrappers with specified URI keys to the current configuration.
     *
     * @param wrappers A [Map] of URI keys to [Wrapper] instances to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addWrappers(wrappers: Map<String, Wrapper>): IConfigBuilder

    /**
     * Removes a wrapper with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the wrapper to remove.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun removeWrapper(uri: String): IConfigBuilder

    /**
     * Adds a package with a specified URI key to the current configuration.
     *
     * @param wrapPackage A [Pair] of the URI key and the [WrapPackage] to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addPackage(wrapPackage: Pair<String, WrapPackage>): IConfigBuilder

    /**
     * Adds a set of packages with specified URI keys to the current configuration.
     *
     * @param packages A [Map] of URI keys to [WrapPackage] instances to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addPackages(packages: Map<String, WrapPackage>): IConfigBuilder

    /**
     * Removes a package with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the package to remove.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun removePackage(uri: String): IConfigBuilder

    /**
     * Adds an environment variable with a specified URI key to the current configuration.
     *
     * @param env A [Pair] of the URI key and the [WrapperEnv] to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addEnv(env: Pair<String, WrapperEnv>): IConfigBuilder

    /**
     * Adds a set of environment variables with specified URI keys to the current configuration.
     *
     * @param envs A [Map] of URI keys to [WrapperEnv] instances to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addEnvs(envs: Map<String, WrapperEnv>): IConfigBuilder

    /**
     * Removes an environment variable with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the environment variable to remove.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun removeEnv(uri: String): IConfigBuilder

    /**
     * Sets or replaces an environment variable with a specified URI key in the current configuration.
     *
     * @param env A [Pair] of the URI key and the [WrapperEnv] to set.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun setEnv(env: Pair<String, WrapperEnv>): IConfigBuilder

    /**
     * Adds an interface implementation with the specified interface and implementation URIs.
     *
     * @param interfaceUri The URI of the interface to associate with the implementation.
     * @param implementationUri The URI of the implementation.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addInterfaceImplementation(interfaceUri: String, implementationUri: String): IConfigBuilder

    /**
     * Adds multiple interface implementations with the specified interface URI and a list of implementation URIs.
     *
     * @param interfaceUri The URI of the interface to associate with the implementations.
     * @param implementationUris A [List] of URIs of the implementations.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addInterfaceImplementations(interfaceUri: String, implementationUris: List<String>): IConfigBuilder

    /**
     * Removes an interface implementation with the specified interface and implementation URIs.
     *
     * @param interfaceUri The URI of the interface associated with the implementation.
     * @param implementationUri The URI of the implementation to remove.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun removeInterfaceImplementation(interfaceUri: String, implementationUri: String): IConfigBuilder

    /**
     * Adds a redirect with a specified source and destination URI.
     *
     * @param redirect A [Pair] of the source URI and the destination URI.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addRedirect(redirect: Pair<String, String>): IConfigBuilder

    /**
     * Adds a set of redirects with specified source and destination URIs.
     *
     * @param redirects A [Map] of source URIs to destination URIs.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addRedirects(redirects: Map<String, String>): IConfigBuilder

    /**
     * Removes a redirect with the specified source URI.
     *
     * @param from The source URI of the redirect to remove.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun removeRedirect(from: String): IConfigBuilder

    /**
     * Adds a [UriResolver] to the current configuration.
     *
     * @param resolver The [UriResolver] instance to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addResolver(resolver: UriResolver): IConfigBuilder

    /**
     * Adds a list of [UriResolver] instances to the current configuration.
     *
     * @param resolvers A [List] of [UriResolver] instances to add.
     * @return This [IConfigBuilder] instance for chaining calls.
     */
    fun addResolvers(resolvers: List<UriResolver>): IConfigBuilder
}
