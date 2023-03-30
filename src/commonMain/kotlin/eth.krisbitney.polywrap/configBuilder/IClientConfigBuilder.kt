package eth.krisbitney.polywrap.configBuilder

import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.types.ClientConfig
import eth.krisbitney.polywrap.core.types.WrapperEnv
import eth.krisbitney.polywrap.uriResolvers.cache.WrapperCache
import eth.krisbitney.polywrap.uriResolvers.embedded.PackageRedirect
import eth.krisbitney.polywrap.uriResolvers.embedded.UriRedirect
import eth.krisbitney.polywrap.uriResolvers.embedded.WrapperRedirect
import kotlin.collections.Map
import kotlin.collections.List

interface IClientConfigBuilder {
    val config: BuilderConfig

    fun build(cache: WrapperCache? = null): ClientConfig

    fun build(resolver: UriResolver): ClientConfig

    fun add(config: BuilderConfig): IClientConfigBuilder

    fun addDefaults(): IClientConfigBuilder

    fun addWrapper(wrapper: WrapperRedirect): IClientConfigBuilder

    fun addWrappers(wrappers: List<WrapperRedirect>): IClientConfigBuilder

    fun removeWrapper(uri: String): IClientConfigBuilder

    fun addPackage(pkg: PackageRedirect): IClientConfigBuilder

    fun addPackages(packages: List<PackageRedirect>): IClientConfigBuilder

    fun removePackage(uri: String): IClientConfigBuilder

    fun addEnv(uri: String, env: WrapperEnv): IClientConfigBuilder

    fun addEnvs(envs: Map<String, WrapperEnv>): IClientConfigBuilder

    fun removeEnv(uri: String): IClientConfigBuilder

    fun setEnv(uri: String, env: WrapperEnv): IClientConfigBuilder

    fun addInterfaceImplementation(interfaceUri: String, implementationUri: String): IClientConfigBuilder

    fun addInterfaceImplementations(interfaceUri: String, implementationUris: List<String>): IClientConfigBuilder

    fun removeInterfaceImplementation(interfaceUri: String, implementationUri: String): IClientConfigBuilder

    fun addRedirect(redirect: UriRedirect): IClientConfigBuilder

    fun addRedirects(redirects: List<UriRedirect>): IClientConfigBuilder

    fun removeRedirect(from: String): IClientConfigBuilder

    fun addResolver(resolver: UriResolver): IClientConfigBuilder

    fun addResolvers(resolvers: List<UriResolver>): IClientConfigBuilder
}
