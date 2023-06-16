package io.polywrap.core

import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import uniffi.main.FfiException
import kotlin.jvm.Throws

/**
 * Client invokes wrappers and interacts with wrap packages.
 */
interface Client {
    /**
     * Retrieves the list of implementation URIs for the specified interface URI.
     *
     * @param uri The URI of the interface for which implementations are being requested.
     * @return A [Result] containing the list of implementation URIs.
     */
    fun getImplementations(uri: String): Result<List<String>>

    /**
     * Returns an env (a set of environmental variables) from the configuration
     * used to instantiate the client.
     * @param uri the URI used to register the env
     * @return an env, or null if an env is not found at the given URI
     */
    fun getEnvByUri(uri: String): Result<WrapperEnv>?

    /**
     * Resolves the [Wrapper] at the specified URI.
     *
     * @param uri The URI of the wrapper to resolve.
     * @param resolutionContext The [UriResolutionContext] to be used during URI resolution, or null for a default context.
     * @return A [Result] containing the [Wrapper], or an error if the resolution fails.
     *
     * @throws FfiException
     *
     * @note The returned result may cause a memory leak if it is not consumed by a call to invokeWrapper
     */
    @Throws(FfiException::class)
    fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Result<Wrapper>

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
    fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ): Result<ByteArray>
}
