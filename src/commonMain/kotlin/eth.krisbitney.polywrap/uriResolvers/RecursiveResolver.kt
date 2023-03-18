package eth.krisbitney.polywrap.uriResolvers

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriPackageOrWrapper
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.types.Client
import eth.krisbitney.polywrap.uriResolvers.util.InfiniteLoopException

/**
 * A [UriResolver] implementation that resolves URIs recursively.
 *
 * @property resolver The [UriResolver] instance used for resolving URIs.
 */
class RecursiveResolver(private val resolver: UriResolver) : UriResolver {

    /**
     * Companion object that provides a factory method for creating [RecursiveResolver] instances.
     */
    companion object {
        /**
         * Creates a [RecursiveResolver] instance from a [UriResolverLike] type.
         *
         * @param resolver The [UriResolverLike] instance to be used for creating a [RecursiveResolver].
         * @return A [RecursiveResolver] instance.
         */
        fun from(resolver: UriResolverLike): RecursiveResolver {
            return RecursiveResolver(UriResolverFactory.from(resolver))
        }
    }

    /**
     * Tries to resolve the given [Uri] recursively by trying to resolve it again if a redirect to another [Uri] occurs.
     *
     * @param uri The [Uri] to resolve.
     * @param client The [Client] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return A [Result] containing a [UriPackageOrWrapper] if the resolution is successful, or an exception if not.
     */
    override suspend fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        if (resolutionContext.isResolving(uri)) {
            return Result.failure(InfiniteLoopException(uri, resolutionContext.getHistory()))
        }
        resolutionContext.startResolving(uri)
        val resolverResult = resolver.tryResolveUri(uri, client, resolutionContext)
        val result = tryResolveUriAgainIfRedirect(resolverResult, uri, client, resolutionContext)
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
     * @return A [Result] containing a [UriPackageOrWrapper] if the resolution is successful, or an exception if not.
     */
    private suspend fun tryResolveUriAgainIfRedirect(
        result: Result<UriPackageOrWrapper>,
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        val uriOrNull = result.getOrNull()
        if (uriOrNull is UriPackageOrWrapper.UriValue && uriOrNull.uri != uri) {
            return tryResolveUri(uriOrNull.uri, client, resolutionContext)
        }
        return result
    }
}