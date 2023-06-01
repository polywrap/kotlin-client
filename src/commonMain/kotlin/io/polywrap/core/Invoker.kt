package io.polywrap.core

import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import uniffi.main.FfiInvokerInterface

interface Invoker : FfiInvokerInterface {
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
    fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray? = null,
        env: ByteArray? = null,
        resolutionContext: UriResolutionContext? = null
    ): Result<ByteArray>
}