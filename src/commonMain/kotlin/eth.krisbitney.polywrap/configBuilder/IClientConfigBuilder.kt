package eth.krisbitney.polywrap.configBuilder

import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.types.ClientConfig
import eth.krisbitney.polywrap.core.types.WrapPackage
import eth.krisbitney.polywrap.core.types.Wrapper
import eth.krisbitney.polywrap.core.types.WrapperEnv
import eth.krisbitney.polywrap.uriResolvers.cache.WrapperCache
import kotlin.collections.Map
import kotlin.collections.List

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
