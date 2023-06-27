package io.polywrap.configBuilder

import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.WrapEnv
import io.polywrap.core.msgpack.EnvSerializer
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.resolution.UriResolver
import uniffi.main.FfiBuilderConfig
import uniffi.main.FfiClient
import uniffi.main.FfiUri

@OptIn(ExperimentalUnsignedTypes::class)
internal class FfiConfigBuilder : AutoCloseable {

    private val ffiBuilderConfig: FfiBuilderConfig = FfiBuilderConfig()

    fun addEnv(uri: String, env: WrapEnv) {
        val ffiUri = FfiUri.fromString(uri)
        val serializedEnv = msgPackEncode(EnvSerializer, env).asUByteArray().toList()
        ffiBuilderConfig.addEnv(ffiUri, serializedEnv)
    }

    fun removeEnv(uri: String) {
        val ffiUri = FfiUri.fromString(uri)
        ffiBuilderConfig.removeEnv(ffiUri)
    }

    fun addInterfaceImplementation(interfaceUri: String, implementationUri: String) {
        val ffiInterfaceUri = FfiUri.fromString(interfaceUri)
        val ffiImplementationUri = FfiUri.fromString(implementationUri)
        ffiBuilderConfig.addInterfaceImplementation(ffiInterfaceUri, ffiImplementationUri)
    }

    fun removeInterfaceImplementation(interfaceUri: String, implementationUri: String) {
        val ffiInterfaceUri = FfiUri.fromString(interfaceUri)
        val ffiImplementationUri = FfiUri.fromString(implementationUri)
        ffiBuilderConfig.removeInterfaceImplementation(ffiInterfaceUri, ffiImplementationUri)
    }

    /**
     * Adds a wrapper with a specified URI key to the current configuration.
     *
     * @param wrapper A [Pair] of the URI key and the [Wrapper] to add.
     * @return This [BaseConfigBuilder] instance for chaining calls.
     */
    fun addWrapper(uri: String, wrapper: Wrapper) {
        val ffiUri = FfiUri.fromString(uri)
        ffiBuilderConfig.addWrapper(ffiUri, wrapper)
    }

    /**
     * Removes a wrapper with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the wrapper to remove.
     * @return This [BaseConfigBuilder] instance for chaining calls.
     */
    fun removeWrapper(uri: String) {
        val ffiUri = FfiUri.fromString(uri)
        ffiBuilderConfig.removeWrapper(ffiUri)
    }

    fun addPackage(uri: String, wrapPackage: WrapPackage) {
        val ffiUri = FfiUri.fromString(uri)
        ffiBuilderConfig.addPackage(ffiUri, wrapPackage)
    }

    fun removePackage(uri: String) {
        val ffiUri = FfiUri.fromString(uri)
        ffiBuilderConfig.removePackage(ffiUri)
    }

    fun addRedirect(from: String, to: String) {
        val fromUri = FfiUri.fromString(from)
        val toUri = FfiUri.fromString(to)
        ffiBuilderConfig.addRedirect(fromUri, toUri)
    }

    fun removeRedirect(from: String) {
        val fromUri = FfiUri.fromString(from)
        ffiBuilderConfig.removeRedirect(fromUri)
    }

    fun addResolver(resolver: UriResolver) {
        ffiBuilderConfig.addResolver(resolver)
    }

    /**
     * Returns a configured [FfiClient] instance.
     */
    fun build(): FfiClient = ffiBuilderConfig.build()

    override fun close() = ffiBuilderConfig.close()
}
