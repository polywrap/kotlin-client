package io.polywrap.configBuilder

import io.polywrap.client.PolywrapClient
import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.WrapperEnv
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolver
import io.polywrap.msgpack.EnvSerializer
import io.polywrap.msgpack.msgPackEncode
import uniffi.main.FfiBuilderConfig

@OptIn(ExperimentalUnsignedTypes::class)
class ConfigBuilder : AutoCloseable {

    private val ffiBuilderConfig: FfiBuilderConfig = FfiBuilderConfig()

    fun addEnv(uri: String, env: WrapperEnv) {
        val ffiUri = Uri.fromString(uri)
        val serializedEnv = msgPackEncode(EnvSerializer, env).toUByteArray().toList()
        ffiBuilderConfig.addEnv(ffiUri, serializedEnv)
    }

    fun removeEnv(uri: String) {
        val ffiUri = Uri.fromString(uri)
        ffiBuilderConfig.removeEnv(ffiUri)
    }

    fun addInterfaceImplementation(interfaceUri: String, implementationUri: String) {
        val ffiInterfaceUri = Uri.fromString(interfaceUri)
        val ffiImplementationUri = Uri.fromString(implementationUri)
        ffiBuilderConfig.addInterfaceImplementation(ffiInterfaceUri, ffiImplementationUri)
    }

    fun removeInterfaceImplementation(interfaceUri: String, implementationUri: String) {
        val ffiInterfaceUri = Uri.fromString(interfaceUri)
        val ffiImplementationUri = Uri.fromString(implementationUri)
        ffiBuilderConfig.removeInterfaceImplementation(ffiInterfaceUri, ffiImplementationUri)
    }

    fun addWrapper(uri: String, wrapper: Wrapper) {
        val ffiUri = Uri.fromString(uri)
        ffiBuilderConfig.addWrapper(ffiUri, wrapper)
    }

    fun removeWrapper(uri: String) {
        val ffiUri = Uri.fromString(uri)
        ffiBuilderConfig.removeWrapper(ffiUri)
    }

    fun addPackage(uri: String, packageWrapper: WrapPackage) {
        val ffiUri = Uri.fromString(uri)
        ffiBuilderConfig.addPackage(ffiUri, packageWrapper)
    }

    fun removePackage(uri: String) {
        val ffiUri = Uri.fromString(uri)
        ffiBuilderConfig.removePackage(ffiUri)
    }

    fun addRedirect(from: String, to: String) {
        val fromUri = Uri.fromString(from)
        val toUri = Uri.fromString(to)
        ffiBuilderConfig.addRedirect(fromUri, toUri)
    }

    fun removeRedirect(from: String) {
        val fromUri = Uri.fromString(from)
        ffiBuilderConfig.removeRedirect(fromUri)
    }

    fun addResolver(resolver: UriResolver) {
        ffiBuilderConfig.addResolver(resolver)
    }

    fun build(configure: (ConfigBuilder.() -> Unit)? = null): PolywrapClient = this.run {
        configure?.let { this.apply(it) }
        PolywrapClient(ffiBuilderConfig.build())
    }

    override fun close() = ffiBuilderConfig.close()
}