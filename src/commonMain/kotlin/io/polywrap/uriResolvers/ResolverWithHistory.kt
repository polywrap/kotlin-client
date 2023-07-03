package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.*
import uniffi.polywrap_native.FfiException
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriPackageOrWrapper
import uniffi.polywrap_native.FfiUriResolutionContext
import uniffi.polywrap_native.FfiUriResolutionStep
import uniffi.polywrap_native.IffiUriPackageOrWrapper
import kotlin.jvm.Throws

/**
 * An abstract class that implements [UriResolver] and provides additional history tracking
 * for each resolution step.
 */
abstract class ResolverWithHistory : UriResolver {

    /**
     * Resolves a URI with history tracking by invoking [_tryResolveUri], tracks the step
     * in [resolutionContext], and returns the result.
     *
     * @param uri The [FfiUri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [FfiUriResolutionContext] for keeping track of the resolution history.
     * @return An [UriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    @Throws(FfiException::class)
    override fun ffiTryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): UriPackageOrWrapper {
        val result = this._tryResolveUri(uri, invoker, resolutionContext)

        FfiUriPackageOrWrapper(result).use {
            resolutionContext.trackStep(
                FfiUriResolutionStep(
                    sourceUri = uri,
                    result = it,
                    description = this.getStepDescription(uri, result),
                    subHistory = null
                )
            )
        }

        return result
    }

    /**
     * Provides a human-readable description of a resolution step.
     *
     * @param uri The URI being resolved.
     * @param result The [Result] containing a wrap package, a wrapper, or a URI if successful.
     * @return A [String] description of the resolution step.
     */
    protected abstract fun getStepDescription(uri: FfiUri, result: UriPackageOrWrapper): String

    /**
     * The actual URI resolution implementation. Must be implemented by subclasses.
     * @param uri The URI to resolve.
     * @param invoker The [Invoker] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @return A [UriPackageOrWrapper] if successful.
     * @throws [Exception] if resolution fails
     */
    @Throws(Exception::class)
    protected abstract fun _tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): UriPackageOrWrapper
}
