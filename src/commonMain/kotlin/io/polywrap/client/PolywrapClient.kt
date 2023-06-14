package io.polywrap.client

import io.polywrap.core.Client
import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import io.polywrap.core.WrapperEnv
import io.polywrap.core.msgpack.EnvSerializer
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import uniffi.main.FfiClient
import uniffi.main.FfiException
import uniffi.main.FfiUri
import kotlin.jvm.Throws

@OptIn(ExperimentalUnsignedTypes::class)
class PolywrapClient(val ffiClient: FfiClient) : Invoker(), Client, AutoCloseable {

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
            args = args?.asUByteArray()?.toList(),
            env = env?.asUByteArray()?.toList(),
            resolutionContext = resolutionContext
        )
    }.map {
        it.toUByteArray().asByteArray()
    }

    override fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ): Result<ByteArray> = runCatching {
        ffiClient.invokeWrapperRaw(
            wrapper = wrapper,
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
     * @return A list of implementation URIs.
     * @throws FfiException
     *
     * @note Each Uri returned is owned by the caller and must be manually deallocated
     */
    @Throws(FfiException::class)
    override fun getImplementations(uri: FfiUri): List<FfiUri> = ffiClient.getImplementations(uri)

    override fun getImplementations(uri: String): Result<List<String>> = runCatching {
        val ffiUri = Uri.fromString(uri)
        val implementations = this.getImplementations(ffiUri)
        val result = implementations.map { it.toStringUri() }
        implementations.forEach { it.close() }
        result
    }

    /**
     * Retrieves the [WrapperEnv] associated with the specified URI.
     *
     * @param uri The URI of the wrapper environment to retrieve.
     * @return The [WrapperEnv] associated with the given URI, or null if not found.
     */
    override fun getEnvByUri(uri: Uri): List<UByte>? = ffiClient.getEnvByUri(uri)

    override fun getEnvByUri(uri: String): Result<WrapperEnv>? {
        val envBytes = runCatching {
            val ffiUri = Uri.fromString(uri)
            this.getEnvByUri(ffiUri)
        }.getOrElse {
            return Result.failure(it)
        }
        return envBytes
            ?.toUByteArray()
            ?.asByteArray()
            ?.let { msgPackDecode(EnvSerializer, it) }
    }

    override fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext?
    ): Result<Wrapper> = runCatching {
        val ffiWrapper = ffiClient.loadWrapper(uri, resolutionContext)
        Wrapper.fromFfi(ffiWrapper)
    }

    override fun close() = ffiClient.close()
}
