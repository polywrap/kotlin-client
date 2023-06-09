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
    abstract fun invokeRaw(
        uri: Uri,
        method: String,
        args: ByteArray? = null,
        env: ByteArray? = null,
        resolutionContext: UriResolutionContext? = null
    ): Result<ByteArray>

    /**
     * Invoke a wrapper using an instance of the wrapper.
     *
     * @param wrapper An instance of a Wrapper to invoke.
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Result] containing a MsgPack encoded byte array or an error.
     */
    abstract fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray? = null,
        env: ByteArray? = null,
        resolutionContext: UriResolutionContext? = null
    ): Result<ByteArray>

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
}
