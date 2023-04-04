package io.polywrap.uriResolvers

import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.resolution.UriResolver
import io.polywrap.core.types.Client
import io.polywrap.uriResolvers.util.InfiniteLoopException

/**
 * A [UriResolver] implementation that resolves URIs recursively.
 *
 * @property resolver The [UriResolver] instance used for resolving URIs.
 */
class RecursiveResolver(private val resolver: UriResolver) : UriResolver {

    /**
     * Tries to resolve the given [Uri] recursively by trying to resolve it again if a redirect to another [Uri] occurs.
     *
     * @param uri The [Uri] to resolve.
     * @param client The [Client] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @param resolveToPackage If true, the resolver will attempt to resolve the URI to a wrap package. If false, the resolver will attempt to resolve the URI to a wrapper.
     * @return A [Result] containing a [UriPackageOrWrapper] if the resolution is successful, or an exception if not.
     */
    override suspend fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        if (resolutionContext.isResolving(uri)) {
            return Result.failure(InfiniteLoopException(uri, resolutionContext.getHistory()))
        }
        resolutionContext.startResolving(uri)
        val resolverResult = resolver.tryResolveUri(uri, client, resolutionContext)
        val result = tryResolveUriAgainIfRedirect(resolverResult, uri, client, resolutionContext, resolveToPackage)
        resolutionContext.stopResolving(uri)
        return result
    }

    /**
     * Tries to resolve the given [Uri] again if a redirect to another [Uri] occurs.
     *
     * @param result The [Result] containing the previous [UriPackageOrWrapper].
     * @param uri The original [Uri] to resolve.
     * @param client The [Client] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @param resolveToPackage If true, the resolver will attempt to resolve the URI to a wrap package. If false, the resolver will attempt to resolve the URI to a wrapper.
     * @return A [Result] containing a [UriPackageOrWrapper] if the resolution is successful, or an exception if not.
     */
    private suspend fun tryResolveUriAgainIfRedirect(
        result: Result<UriPackageOrWrapper>,
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        val uriOrNull = result.getOrNull()
        if (uriOrNull is UriPackageOrWrapper.UriValue && uriOrNull.uri != uri) {
            return tryResolveUri(uriOrNull.uri, client, resolutionContext, resolveToPackage)
        }
        return result
    }
}
