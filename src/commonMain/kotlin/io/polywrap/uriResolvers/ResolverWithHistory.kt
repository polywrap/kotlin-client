package io.polywrap.uriResolvers

import io.polywrap.core.Uri
import io.polywrap.core.UriResolutionContext
import io.polywrap.core.UriResolutionStep
import io.polywrap.core.resolution.*
import io.polywrap.core.types.Client

/**
 * An abstract class that implements [UriResolver] and provides additional history tracking
 * for each resolution step.
 */
abstract class ResolverWithHistory : UriResolver {

    /**
     * Resolves a URI with history tracking by invoking [_tryResolveUri], tracks the step
     * in [resolutionContext], and returns the result.
     *
     * @param uri The URI to resolve.
     * @param client The [Client] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @param resolveToPackage If true, the resolver will attempt to resolve the URI to a wrap package. If false, the resolver will attempt to resolve the URI to a wrapper.
     * @return A [Result] containing a wrap package, a wrapper, or a URI if successful.
     */
    override fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        val result = this._tryResolveUri(uri, client, resolutionContext, resolveToPackage)

        resolutionContext.trackStep(
            UriResolutionStep(
                sourceUri = uri,
                result = result,
                description = this.getStepDescription(uri, result)
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
    protected abstract fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String

    /**
     * The actual URI resolution implementation. Must be implemented by subclasses.
     * @param uri The URI to resolve.
     * @param client The [Client] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @param resolveToPackage If true, the resolver will attempt to resolve the URI to a wrap package. If false, the resolver will attempt to resolve the URI to a wrapper.
     * @return A [Result] containing a wrap package, a wrapper, or a URI if successful.
     */
    protected abstract fun _tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper>
}
