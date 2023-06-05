package io.polywrap.client

import io.polywrap.core.Client
import io.polywrap.core.InvokeResult
import io.polywrap.core.Wrapper
import io.polywrap.core.WrapperEnv
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.msgpack.EnvSerializer
import io.polywrap.core.msgpack.MapArgsSerializer
import io.polywrap.core.msgpack.NullableKVSerializer
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import kotlinx.serialization.serializer
import uniffi.main.FfiClient
import uniffi.main.FfiException
import uniffi.main.FfiUri

@OptIn(ExperimentalUnsignedTypes::class)
class PolywrapClient(val ffiClient: FfiClient) : Client, AutoCloseable {

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
    override fun invokeRaw(
        uri: FfiUri,
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        resolutionContext: UriResolutionContext?
    ): List<UByte> = ffiClient.invokeRaw(
        uri = uri,
        method = method,
        args = args,
        env = env,
        resolutionContext = resolutionContext
    )

    override fun invokeRaw(
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ) = runCatching {
            ffiClient.invokeRaw(
                uri = uri,
                method = method,
                args = args?.toUByteArray()?.toList(),
                env = env?.toUByteArray()?.toList(),
                resolutionContext = resolutionContext
            )
        }.map {
            it.toUByteArray().toByteArray()
        }

    override fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ): Result<ByteArray> =  runCatching {
        ffiClient.invokeWrapperRaw(
            wrapper = wrapper,
            uri = uri,
            method = method,
            args = args?.toUByteArray()?.toList(),
            env = env?.toUByteArray()?.toList(),
            resolutionContext = resolutionContext
        )
    }.map {
        it.toUByteArray().toByteArray()
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

    /**
     * Returns the interface implementations stored in the configuration.
     *
     * @return A map of interface URIs to a list of their respective implementation URIs.
     *
     * @note Each Uri returned is owned by the caller and must be manually deallocated
     */
    override fun getInterfaces(): Map<String, List<FfiUri>>? = ffiClient.getInterfaces()

    /**
     * Retrieves the list of implementation URIs for the specified interface URI.
     *
     * @param uri The URI of the interface for which implementations are being requested.
     * @return A [Result] containing the list of implementation URIs.
     * @throws FfiException
     *
     * @note Each Uri returned is owned by the caller and must be manually deallocated
     */
    override fun getImplementations(uri: FfiUri): List<FfiUri> = ffiClient.getImplementations(uri)

    /**
     * Retrieves the [WrapperEnv] associated with the specified URI.
     *
     * @param uri The URI of the wrapper environment to retrieve.
     * @return The [WrapperEnv] associated with the given URI, or null if not found.
     */
    override fun getEnvByUri(uri: Uri): List<UByte>? = ffiClient.getEnvByUri(uri)

    override fun getEnvByUri(uri: String): WrapperEnv? = Uri.fromString(uri).let { ffiUri ->
        ffiClient.getEnvByUri(ffiUri)
            ?.toUByteArray()
            ?.toByteArray()
            ?.let { msgPackDecode(EnvSerializer, it) }
            ?.getOrThrow()
    }

    override fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext?
    ): Result<Wrapper> = runCatching {
        ffiClient.loadWrapper(uri, resolutionContext)
    }

    override fun close() = ffiClient.close()
}
