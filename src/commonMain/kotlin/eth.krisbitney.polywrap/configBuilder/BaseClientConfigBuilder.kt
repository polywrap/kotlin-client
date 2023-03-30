package eth.krisbitney.polywrap.configBuilder

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.types.ClientConfig
import eth.krisbitney.polywrap.core.types.WrapperEnv
import eth.krisbitney.polywrap.uriResolvers.cache.WrapperCache
import eth.krisbitney.polywrap.uriResolvers.embedded.PackageRedirect
import eth.krisbitney.polywrap.uriResolvers.embedded.UriRedirect
import eth.krisbitney.polywrap.uriResolvers.embedded.WrapperRedirect
import kotlin.collections.Map

abstract class BaseClientConfigBuilder : IClientConfigBuilder {
    override val config = BuilderConfig(
        envs = mutableMapOf(),
        interfaces = mutableMapOf(),
        redirects = mutableMapOf(),
        wrappers = mutableMapOf(),
        packages = mutableMapOf(),
        resolvers = mutableListOf()
    )

    abstract override fun addDefaults(): IClientConfigBuilder
    abstract override fun build(cache: WrapperCache?): ClientConfig
    abstract override fun build(resolver: UriResolver): ClientConfig


    override fun add(config: BuilderConfig): IClientConfigBuilder = this.apply {
        addEnvs(config.envs)
        config.redirects.forEach { addRedirect(Uri(it.key) to Uri(it.value)) }
        config.wrappers.forEach { addWrapper(Uri(it.key) to it.value) }
        config.packages.forEach { addPackage(Uri(it.key) to it.value) }
        config.interfaces.forEach { (interfaceUri, implementations) ->
            addInterfaceImplementations(interfaceUri, implementations.toList())
        }
        addResolvers(config.resolvers)
    }

    override fun addWrapper(wrapper: WrapperRedirect): IClientConfigBuilder = this.apply {
        config.wrappers[wrapper.first.uri] = wrapper.second
    }

    override fun addWrappers(wrappers: List<WrapperRedirect>): IClientConfigBuilder = this.apply {
        wrappers.forEach { addWrapper(it) }
    }

    override fun removeWrapper(uri: String): IClientConfigBuilder = this.apply {
        config.wrappers.remove(Uri(uri).uri)
    }

    override fun addPackage(pkg: PackageRedirect): IClientConfigBuilder = this.apply {
        config.packages[pkg.first.uri] = pkg.second
    }

    override fun addPackages(packages: List<PackageRedirect>): IClientConfigBuilder = this.apply {
        packages.forEach { addPackage(it) }
    }

    override fun removePackage(uri: String): IClientConfigBuilder = this.apply {
        config.packages.remove(Uri(uri).uri)
    }

    override fun addEnv(uri: String, env: WrapperEnv): IClientConfigBuilder = this.apply {
        val sanitizedUri = Uri(uri).uri
        config.envs[sanitizedUri] = (config.envs[sanitizedUri] ?: emptyMap()) + env
    }

    override fun addEnvs(envs: Map<String, WrapperEnv>): IClientConfigBuilder = this.apply {
        envs.forEach { (uri, env) -> addEnv(uri, env) }
    }

    override fun removeEnv(uri: String): IClientConfigBuilder = this.apply {
        config.envs.remove(Uri(uri).uri)
    }

    override fun setEnv(uri: String, env: WrapperEnv): IClientConfigBuilder = this.apply {
        config.envs[Uri(uri).uri] = env
    }

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

    override fun addRedirect(redirect: UriRedirect): IClientConfigBuilder = this.apply {
        this.config.redirects[redirect.first.uri] = redirect.second.uri
    }

    override fun addRedirects(redirects: List<UriRedirect>): IClientConfigBuilder = this.apply {
        redirects.forEach { addRedirect(it) }
    }

    override fun removeRedirect(from: String): IClientConfigBuilder = this.apply {
        this.config.redirects.remove(Uri(from).uri)
    }

    override fun addResolver(resolver: UriResolver): IClientConfigBuilder = this.apply {
        this.config.resolvers.add(resolver)
    }

    override fun addResolvers(resolvers: List<UriResolver>): IClientConfigBuilder = this.apply {
        resolvers.forEach { addResolver(it) }
    }
}