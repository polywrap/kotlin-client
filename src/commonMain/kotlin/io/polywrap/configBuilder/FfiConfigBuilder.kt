package io.polywrap.configBuilder

import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.WrapEnv
import io.polywrap.core.msgpack.EnvSerializer
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.resolution.UriResolver
import uniffi.polywrap_native.FfiBuilderConfig
import uniffi.polywrap_native.FfiClient
import uniffi.polywrap_native.FfiUri

@OptIn(ExperimentalUnsignedTypes::class)
internal class FfiConfigBuilder : AutoCloseable {

    private val ffiBuilderConfig: FfiBuilderConfig = FfiBuilderConfig()

    fun addEnv(uri: String, env: WrapEnv) {
        FfiUri.fromString(uri).use { ffiUri ->
            val serializedEnv = msgPackEncode(EnvSerializer, env).asUByteArray().toList()
            ffiBuilderConfig.addEnv(ffiUri, serializedEnv)
        }
    }

    fun removeEnv(uri: String) {
        FfiUri.fromString(uri).use { ffiUri ->
            ffiBuilderConfig.removeEnv(ffiUri)
        }
    }

    fun addInterfaceImplementation(interfaceUri: String, implementationUri: String) {
        FfiUri.fromString(interfaceUri).use {ffiInterfaceUri ->
            FfiUri.fromString(implementationUri).use { ffiImplementationUri ->
                ffiBuilderConfig.addInterfaceImplementation(ffiInterfaceUri, ffiImplementationUri)
            }
        }
    }

    fun removeInterfaceImplementation(interfaceUri: String, implementationUri: String) {
        FfiUri.fromString(interfaceUri).use { ffiInterfaceUri ->
            FfiUri.fromString(implementationUri).use { ffiImplementationUri ->
                ffiBuilderConfig.removeInterfaceImplementation(ffiInterfaceUri, ffiImplementationUri)
            }
        }
    }

    /**
     * Adds a wrapper with a specified URI key to the current configuration.
     *
     * @param wrapper A [Pair] of the URI key and the [Wrapper] to add.
     * @return This [BaseConfigBuilder] instance for chaining calls.
     */
    fun addWrapper(uri: String, wrapper: Wrapper) {
        FfiUri.fromString(uri).use { ffiUri ->
            when (wrapper is AutoCloseable) {
                true -> wrapper.use {
                    ffiBuilderConfig.addWrapper(ffiUri, wrapper)
                }
                false -> ffiBuilderConfig.addWrapper(ffiUri, wrapper)
            }
        }
    }

    /**
     * Removes a wrapper with the specified URI key from the current configuration.
     *
     * @param uri The URI key of the wrapper to remove.
     * @return This [BaseConfigBuilder] instance for chaining calls.
     */
    fun removeWrapper(uri: String) {
        FfiUri.fromString(uri).use { ffiUri ->
            ffiBuilderConfig.removeWrapper(ffiUri)
        }
    }

    fun addPackage(uri: String, wrapPackage: WrapPackage) {
        FfiUri.fromString(uri).use { ffiUri ->
            ffiBuilderConfig.addPackage(ffiUri, wrapPackage)
        }
    }

    fun removePackage(uri: String) {
        FfiUri.fromString(uri).use { ffiUri ->
            ffiBuilderConfig.removePackage(ffiUri)
        }
    }

    fun addRedirect(from: String, to: String) {
        FfiUri.fromString(from).use { fromUri ->
            FfiUri.fromString(to).use { toUri ->
                ffiBuilderConfig.addRedirect(fromUri, toUri)
            }
        }
    }

    fun removeRedirect(from: String) {
        FfiUri.fromString(from).use { ffiUri ->
            ffiBuilderConfig.removeRedirect(ffiUri)
        }
    }

    fun addResolver(resolver: UriResolver) {
        resolver.use {
            ffiBuilderConfig.addResolver(it)
        }
    }

    /**
     * Returns a configured [FfiClient] instance.
     */
    fun build(): FfiClient = ffiBuilderConfig.build()

    override fun close() = ffiBuilderConfig.close()
}
