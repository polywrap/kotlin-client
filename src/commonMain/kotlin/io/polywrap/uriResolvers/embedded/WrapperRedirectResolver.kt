package io.polywrap.uriResolvers.embedded

import io.polywrap.core.resolution.*
import io.polywrap.core.types.Client
import io.polywrap.core.types.Wrapper
import io.polywrap.uriResolvers.ResolverWithHistory

/**
 * A concrete implementation of [ResolverWithHistory] that resolves a URI using a [Wrapper].
 *
 * @property from The URI associated with the wrapper.
 * @property wrapper The [Wrapper] instance used for resolution.
 */
class WrapperRedirectResolver(val from: Uri, val wrapper: Wrapper) : ResolverWithHistory() {

    /**
     * Returns a human-readable description of the resolution step for this [WrapperRedirectResolver].
     *
     * @param uri The URI being resolved.
     * @param result The [Result] containing a wrap package, a wrapper, or a URI if successful.
     * @return A [String] description of the resolution step.
     */
    override fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String =
        "Wrapper (${from.uri})"

    /**
     * Tries to resolve the given [uri] using the associated wrapper.
     *
     * @param uri The URI to resolve.
     * @param client The [Client] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @param resolveToPackage Whether to resolve the URI to a wrap package (ignored by this resolver).
     * @return A [Result] containing a wrap package, a wrapper, or a URI if successful.
     */
    override fun _tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        return if (uri.uri != from.uri) {
            Result.success(UriPackageOrWrapper.UriValue(uri))
        } else {
            Result.success(UriPackageOrWrapper.WrapperValue(from, wrapper))
        }
    }
}
