package io.polywrap.core

import io.polywrap.core.msgpack.EnvSerializer
import io.polywrap.core.msgpack.MapArgsSerializer
import io.polywrap.core.msgpack.NullableKVSerializer
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import kotlinx.serialization.serializer
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import uniffi.main.FfiUriResolutionContext

abstract class Invoker : FfiInvoker {
    /**
     * Invoke a wrapper. Unlike [invokeWrapperRaw], this method automatically retrieves and caches the wrapper.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Result] containing the invocation result as a [ByteArray], or an error if the invocation fails.
     */
    fun invokeRaw(
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ): Result<ByteArray> = runCatching {
        this.invokeRaw(
            uri = uri,
            method = method,
            args = args?.asUByteArray()?.toList(),
            env = env?.asUByteArray()?.toList(),
            resolutionContext = resolutionContext
        )
    }.map {
        it.toUByteArray().asByteArray()
    }

    /**
     * Invokes the wrapper at the specified URI with the provided method, arguments, and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args A map of arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified R> invoke(
        uri: Uri,
        method: String,
        args: Map<String, Any?>? = null,
        env: Map<String, Any>? = null,
        resolutionContext: UriResolutionContext? = null
    ): InvokeResult<R> = invokeRaw(
        uri = uri,
        method = method,
        args = args?.let { msgPackEncode(MapArgsSerializer, it) },
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
     * Invokes the wrapper at the specified URI with the provided method and arguments of type [T], and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args An instance of type [T] representing the arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified T, reified R> invoke(
        uri: Uri,
        method: String,
        args: T? = null,
        env: Map<String, Any>? = null,
        resolutionContext: UriResolutionContext? = null
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

    companion object {
        /**
         * Creates an [Invoker] from an [FfiInvoker].
         *
         * @param ffiInvoker The [FfiInvoker] to be wrapped.
         * @return An [Invoker] that wraps the provided [FfiInvoker].
         */
        fun fromFfi(ffiInvoker: FfiInvoker): Invoker = object : Invoker() {
            override fun invokeRaw(
                uri: FfiUri,
                method: String,
                args: List<UByte>?,
                env: List<UByte>?,
                resolutionContext: FfiUriResolutionContext?
            ): List<UByte> = ffiInvoker.invokeRaw(uri, method, args, env, resolutionContext)

            override fun getImplementations(uri: FfiUri): List<FfiUri> {
                return ffiInvoker.getImplementations(uri)
            }

            override fun getInterfaces(): Map<String, List<FfiUri>>? {
                return ffiInvoker.getInterfaces()
            }

            override fun getEnvByUri(uri: FfiUri): List<UByte>? {
                return ffiInvoker.getEnvByUri(uri)
            }
        }
    }
}
