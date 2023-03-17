package eth.krisbitney.polywrap.uriResolvers

import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriPackageOrWrapper
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.uriResolvers.util.ResolverWithHistory

/**
 * A concrete implementation of [ResolverWithHistory] that redirects a URI to another URI.
 *
 * @property from The URI to redirect from.
 * @property to The URI to redirect to.
 */
class UriRedirectResolver(val from: Uri, val to: Uri) : ResolverWithHistory() {

    /**
     * Returns a human-readable description of the resolution step for this [UriRedirectResolver].
     *
     * @param uri The URI being resolved.
     * @param result The [Result] containing a wrap package, a wrapper, or a URI if successful.
     * @return A [String] description of the resolution step.
     */
    override suspend fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String =
        "Redirect (${this.from.uri} - ${this.to.uri})"

    /**
     * Tries to resolve the given [uri] by redirecting it to another URI.
     *
     * @param uri The URI to resolve.
     * @param invoker The [Invoker] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @return A [Result] containing a wrap package, a wrapper, or a URI if successful.
     */
    override suspend fun _tryResolveUri(
        uri: Uri,
        invoker: Invoker,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        return if (uri.uri != this.from.uri) {
            Result.success(UriPackageOrWrapper.UriValue(uri))
        } else {
            Result.success(UriPackageOrWrapper.UriValue(to))
        }
    }
}