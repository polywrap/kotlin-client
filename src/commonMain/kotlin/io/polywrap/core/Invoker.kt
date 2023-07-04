package io.polywrap.core

import io.polywrap.core.msgpack.EnvSerializer
import io.polywrap.core.msgpack.NullableKVSerializer
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.resolution.Uri
import kotlinx.serialization.serializer
import uniffi.polywrap_native.FfiException
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriResolutionContext
import kotlin.jvm.Throws

@OptIn(ExperimentalUnsignedTypes::class)
open class Invoker(val ffiInvoker: FfiInvoker) : WrapInvoker, AutoCloseable {

    @Throws(FfiException::class)
    override fun invokeRaw(
        uri: FfiUri,
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        resolutionContext: FfiUriResolutionContext?
    ): List<UByte> = uri.use {
        ffiInvoker.invokeRaw(
            uri = uri,
            method = method,
            args = args,
            env = env,
            resolutionContext = resolutionContext
        )
    }

    override fun invokeRaw(
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: FfiUriResolutionContext?
    ): Result<ByteArray> = runCatching {
        uri.toFfi().use {
            ffiInvoker.invokeRaw(
                uri = it,
                method = method,
                args = args?.asUByteArray()?.toList(),
                env = env?.asUByteArray()?.toList(),
                resolutionContext = resolutionContext
            )
        }
    }.map {
        it.toUByteArray().asByteArray()
    }

    /**
     * Invokes the wrapper at the specified URI with the provided method and arguments of type [T], and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args An instance of type [T] representing the arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [FfiUriResolutionContext] to be used during URI resolution, or null for a default context.
     * The caller owns resolutionContext and is responsible for closing it to prevent a memory leak.
     * @return An [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified T, reified R> invoke(
        uri: Uri,
        method: String,
        args: T? = null,
        env: WrapEnv? = null,
        resolutionContext: FfiUriResolutionContext? = null
    ): InvokeResult<R> = invokeRaw(
        uri = uri,
        method = method,
        args = args?.let { msgPackEncode(serializer<T>(), it) },
        env = env?.let { msgPackEncode(EnvSerializer, it) },
        resolutionContext = resolutionContext
    ).mapCatching {
        if (R::class == Map::class) {
            msgPackDecode(NullableKVSerializer, it).getOrThrow() as R
        } else {
            msgPackDecode(serializer<R>(), it).getOrThrow()
        }
    }

    /**
     * Invokes the wrapper at the specified URI with the provided method, arguments, and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args A map of arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [FfiUriResolutionContext] to be used during URI resolution, or null for a default context.
     * The caller owns resolutionContext and is responsible for closing it to prevent a memory leak.
     * @return An [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified R> invoke(
        uri: Uri,
        method: String,
        args: Map<String, Any?>? = null,
        env: WrapEnv? = null,
        resolutionContext: FfiUriResolutionContext? = null
    ): InvokeResult<R> = invokeRaw(
        uri = uri,
        method = method,
        args = args?.let { msgPackEncode(NullableKVSerializer, it) },
        env = env?.let { msgPackEncode(EnvSerializer, it) },
        resolutionContext = resolutionContext
    ).mapCatching {
        if (R::class == Map::class) {
            msgPackDecode(NullableKVSerializer, it).getOrThrow() as R
        } else {
            msgPackDecode(serializer<R>(), it).getOrThrow()
        }
    }

    override fun getInterfaces(): Map<Uri, List<Uri>>? {
        return ffiInvoker.getInterfaces()?.let { ffiInterfaces ->
            val result: MutableMap<Uri, List<Uri>> = mutableMapOf()
            ffiInterfaces.forEach { (interfaceUri, implementations) ->
                val key = Uri(interfaceUri)
                val value = implementations.map { Uri(it) }
                result[key] = value
            }
            result
        }
    }

    override fun getImplementations(uri: Uri): Result<List<Uri>> = runCatching {
        val implementations = uri.toFfi().use { ffiInvoker.getImplementations(it) }
        val result = implementations.map { Uri(it) }
        result
    }

    override fun getEnvByUri(uri: Uri): Result<WrapEnv?> {
        val envBytes = runCatching {
            uri.toFfi().use { ffiInvoker.getEnvByUri(it) }
        }.getOrElse {
            return Result.failure(it)
        } ?: return Result.success(null)

        return envBytes
            .toUByteArray()
            .asByteArray()
            .let { msgPackDecode(it) }
    }

    override fun close() = ffiInvoker.close()
}
