package io.polywrap.configBuilder

import io.polywrap.core.resolution.UriResolver
import io.polywrap.core.types.ClientConfig
import io.polywrap.core.types.WrapPackage
import io.polywrap.core.types.Wrapper
import io.polywrap.core.types.WrapperEnv
import io.polywrap.uriResolvers.cache.WrapperCache
import kotlin.collections.List
import kotlin.collections.Map

interface IClientConfigBuilder {
    val config: BuilderConfig

    fun build(cache: WrapperCache? = null): ClientConfig

    fun build(resolver: UriResolver): ClientConfig

    fun add(config: BuilderConfig): IClientConfigBuilder

    fun addDefaults(): IClientConfigBuilder

    fun addWrapper(wrapper: Pair<String, Wrapper>): IClientConfigBuilder

    fun addWrappers(wrappers: Map<String, Wrapper>): IClientConfigBuilder

    fun removeWrapper(uri: String): IClientConfigBuilder

    fun addPackage(wrapPackage: Pair<String, WrapPackage>): IClientConfigBuilder

    fun addPackages(packages: Map<String, WrapPackage>): IClientConfigBuilder

    fun removePackage(uri: String): IClientConfigBuilder

    fun addEnv(env: Pair<String, WrapperEnv>): IClientConfigBuilder

    fun addEnvs(envs: Map<String, WrapperEnv>): IClientConfigBuilder

    fun removeEnv(uri: String): IClientConfigBuilder

    fun setEnv(env: Pair<String, WrapperEnv>): IClientConfigBuilder

    fun addInterfaceImplementation(interfaceUri: String, implementationUri: String): IClientConfigBuilder

    fun addInterfaceImplementations(interfaceUri: String, implementationUris: List<String>): IClientConfigBuilder

    fun removeInterfaceImplementation(interfaceUri: String, implementationUri: String): IClientConfigBuilder

    fun addRedirect(redirect: Pair<String, String>): IClientConfigBuilder

    fun addRedirects(redirects: Map<String, String>): IClientConfigBuilder

    fun removeRedirect(from: String): IClientConfigBuilder

    fun addResolver(resolver: UriResolver): IClientConfigBuilder

    fun addResolvers(resolvers: List<UriResolver>): IClientConfigBuilder
}
