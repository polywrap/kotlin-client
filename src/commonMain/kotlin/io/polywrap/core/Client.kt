package io.polywrap.core

import io.polywrap.core.resolution.Uri
import uniffi.main.FfiException
import uniffi.main.FfiUriResolutionContext
import kotlin.jvm.Throws

/**
 * Client invokes wrappers and interacts with wrap packages.
 */
interface Client {
    /**
     * Resolves the [Wrapper] at the specified URI.
     *
     * @param uri The URI of the wrapper to resolve.
     * @param resolutionContext The [FfiUriResolutionContext] to be used during URI resolution, or null for a default context.
     * The caller owns resolutionContext and is responsible for closing it to prevent a memory leak.
     * @return A [Result] containing the [Wrapper], or an error if the resolution fails.
     *
     * @throws FfiException
     */
    @Throws(FfiException::class)
    fun loadWrapper(
        uri: Uri,
        resolutionContext: FfiUriResolutionContext? = null
    ): Result<Wrapper>

    /**
     * Invoke a wrapper using an instance of the wrapper.
     *
     * @param wrapper An instance of a Wrapper to invoke.
     * @param uri The URI of the wrapper to be invoked.
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param resolutionContext The [FfiUriResolutionContext] to be used during URI resolution, or null for a default context.
     * The caller owns resolutionContext and is responsible for closing it to prevent a memory leak.
     * @return A [Result] containing a MsgPack encoded byte array or an error.
     */
    fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: FfiUriResolutionContext?
    ): Result<ByteArray>
}
