package eth.krisbitney.polywrap.uriResolvers.util

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.Invoker

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
     * @param invoker The [Invoker] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @return A [Result] containing a wrap package, a wrapper, or a URI if successful.
     */
    override suspend fun tryResolveUri(uri: Uri, invoker: Invoker, resolutionContext: UriResolutionContext): Result<UriPackageOrWrapper> {
        val result = this._tryResolveUri(uri, invoker, resolutionContext)

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
    protected abstract suspend fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String

    /**
     * The actual URI resolution implementation. Must be implemented by subclasses.
     * @param uri The URI to resolve.
     * @param invoker The [Invoker] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @return A [Result] containing a wrap package, a wrapper, or a URI if successful.
     */
    protected abstract suspend fun _tryResolveUri(uri: Uri, invoker: Invoker, resolutionContext: UriResolutionContext): Result<UriPackageOrWrapper>
}