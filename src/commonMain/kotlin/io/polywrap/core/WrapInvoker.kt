package io.polywrap.core

import io.polywrap.core.resolution.Uri
import uniffi.polywrap_native.FfiException
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriResolutionContext
import kotlin.jvm.Throws

interface WrapInvoker {
    /**
     * Invokes the wrapper at the specified URI with the provided options.
     *
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param resolutionContext The [FfiUriResolutionContext] to be used during URI resolution, or null for a default context.
     * The caller owns resolutionContext and is responsible for closing it to prevent a memory leak.
     * @return A list of MessagePack-encoded bytes representing the invocation result
     * @throws FfiException
     */
    @Throws(FfiException::class)
    fun invokeRaw(
        uri: FfiUri,
        method: String,
        args: List<UByte>? = null,
        env: List<UByte>? = null,
        resolutionContext: FfiUriResolutionContext? = null
    ): List<UByte>

        /**
         * Invoke a wrapper. This method automatically retrieves and caches the wrapper.
         *
         * @param uri The URI of the wrapper to be invoked.
         * @param method The method to be called on the wrapper.
         * @param args Arguments for the method, encoded in the MessagePack byte format
         * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
         * @param resolutionContext The [FfiUriResolutionContext] to be used during URI resolution, or null for a default context.
         * The caller owns resolutionContext and is responsible for closing it to prevent a memory leak.
         * @return A [Result] containing the invocation result as a [ByteArray], or an error if the invocation fails.
         */
        fun invokeRaw(
            uri: Uri,
            method: String,
            args: ByteArray?,
            env: ByteArray? = null,
            resolutionContext: FfiUriResolutionContext? = null
        ): Result<ByteArray>

        /**
         * Returns the interface implementations stored in the configuration.
         *
         * @return A map of interface URIs to a list of their respective implementation URIs.
         */
        fun getInterfaces(): Map<Uri, List<Uri>>?

        /**
         * Retrieves the list of implementation URIs for the specified interface URI.
         *
         * @param uri The URI of the interface for which implementations are being requested.
         * @return A [Result] containing the list of implementation URIs.
         */
        fun getImplementations(uri: Uri): Result<List<Uri>>

        /**
         * Returns an env (a set of environmental variables) from the configuration
         * used to instantiate the client.
         * @param uri the URI used to register the env
         * @return an env, or null if an env is not found at the given URI
         */
        fun getEnvByUri(uri: Uri): Result<WrapEnv?>
}