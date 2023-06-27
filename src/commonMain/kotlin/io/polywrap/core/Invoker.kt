package io.polywrap.core

import io.polywrap.core.msgpack.EnvSerializer
import io.polywrap.core.msgpack.MapArgsSerializer
import io.polywrap.core.msgpack.NullableKVSerializer
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import kotlinx.serialization.serializer
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import kotlin.jvm.Throws

@OptIn(ExperimentalUnsignedTypes::class)
open class Invoker(val ffiInvoker: FfiInvoker) {

    /**
     * Invokes the wrapper at the specified URI with the provided options.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A list of MessagePack-encoded bytes representing the invocation result
     * @throws FfiException
     */
    @Throws(FfiException::class)
    fun invokeRaw(
        uri: FfiUri,
        method: String,
        args: List<UByte>? = null,
        env: List<UByte>? = null,
        resolutionContext: UriResolutionContext? = null
    ): List<UByte> = ffiInvoker.invokeRaw(
        uri = uri,
        method = method,
        args = args,
        env = env,
        resolutionContext = resolutionContext
    )

    /**
     * Invoke a wrapper. This method automatically retrieves and caches the wrapper.
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
        env: ByteArray? = null,
        resolutionContext: UriResolutionContext? = null
    ): Result<ByteArray> = runCatching {
        ffiInvoker.invokeRaw(
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
     * Invokes the wrapper at the specified URI with the provided method and arguments of type [T], and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args An instance of type [T] representing the arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return An [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified T, reified R> invoke(
        uri: Uri,
        method: String,
        args: T? = null,
        env: WrapEnv? = null,
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

    /**
     * Invokes the wrapper at the specified URI with the provided method, arguments, and environment.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args A map of arguments to be passed to the method.
     * @param env A map representing the environment to be used during the invocation.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return An [InvokeResult] containing the invocation result of type [R], or an error if the invocation fails.
     */
    inline fun <reified R> invoke(
        uri: Uri,
        method: String,
        args: Map<String, Any?>? = null,
        env: WrapEnv? = null,
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
     * Returns the interface implementations stored in the configuration.
     *
     * @return A map of interface URIs to a list of their respective implementation URIs.
     */
    fun getInterfaces(): Map<String, List<String>>? {
        val interfaces = ffiInvoker.getInterfaces()
        val result = interfaces?.mapValues { (_, implementations) ->
            implementations.map { it.toStringUri() }
        }
        interfaces?.values?.forEach { implementations ->
            implementations.forEach { it.close() }
        }
        return result
    }

    /**
     * Retrieves the list of implementation URIs for the specified interface URI.
     *
     * @param uri The URI of the interface for which implementations are being requested.
     * @return A [Result] containing the list of implementation URIs.
     */
    fun getImplementations(uri: String): Result<List<String>> = runCatching {
        val ffiUri = Uri.fromString(uri)
        val implementations = ffiInvoker.getImplementations(ffiUri)
        val result = implementations.map { it.toStringUri() }
        implementations.forEach { it.close() }
        result
    }

    /**
     * Returns an env (a set of environmental variables) from the configuration
     * used to instantiate the client.
     * @param uri the URI used to register the env
     * @return an env, or null if an env is not found at the given URI
     */
    fun getEnvByUri(uri: String): Result<WrapEnv?> {
        val envBytes = runCatching {
            val ffiUri = Uri.fromString(uri)
            ffiInvoker.getEnvByUri(ffiUri)
        }.getOrElse {
            return Result.failure(it)
        } ?: return Result.success(null)

        return envBytes
            .toUByteArray()
            .asByteArray()
            .let { msgPackDecode(it) }
    }
}
