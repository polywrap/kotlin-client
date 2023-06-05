package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.*
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriResolutionContext

/**
 * An abstract class that implements [UriResolver] and provides additional history tracking
 * for each resolution step.
 */
abstract class ResolverWithHistory : UriResolver {

    /**
     * Resolves a URI with history tracking by invoking [_tryResolveUri], tracks the step
     * in [resolutionContext], and returns the result.
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    override fun tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper {
        val result = this._tryResolveUri(uri, invoker, resolutionContext, false)

        resolutionContext.trackStep(
            UriResolutionStep(
                sourceUri = uri,
                result = result,
                description = this.getStepDescription(uri, result),
                subHistory = null
            )
        )

        return result
    }

    override fun tryResolveUriToPackage(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper {
        val result = this._tryResolveUri(uri, invoker, resolutionContext, true)

        resolutionContext.trackStep(
            UriResolutionStep(
                sourceUri = uri,
                result = result,
                description = this.getStepDescription(uri, result),
                subHistory = null
            )
        )

        return result
    }

    /**
     * Provides a human-readable description of a resolution step.
     *
     * @param uri The URI being resolved.
     * @param result The [Result] containing a wrap package, a wrapper, or a URI if successful.
     * @return A [String] description of the resolution step.
     */
    protected abstract fun getStepDescription(uri: Uri, result: FfiUriPackageOrWrapper): String

    /**
     * The actual URI resolution implementation. Must be implemented by subclasses.
     * @param uri The URI to resolve.
     * @param invoker The [Invoker] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @param resolveToPackage If true, the resolver will attempt to resolve the URI to a wrap package. If false, the resolver will attempt to resolve the URI to a wrapper.
     * @return A [UriPackageOrWrapper] if successful.
     * @throws [Exception] if resolution fails
     */
    protected abstract fun _tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext,
        resolveToPackage: Boolean
    ): FfiUriPackageOrWrapper
}
