package eth.krisbitney.polywrap.core.resolution

import kotlinx.coroutines.Deferred

/**
 * An entity capable of resolving a wrap URI, typically by using an IUriResolver implementation.
 */
interface UriResolutionHandler {
    /**
     * Resolve a URI to a wrap package, a wrapper, or a URI.
     *
     * @param uri The URI to resolve.
     * @param resolutionContext The current URI resolution context (optional).
     * @return A Result containing either a wrap package, a wrapper, or a URI if successful.
     */
    suspend fun tryResolveUri(
        uri: Uri,
        resolutionContext: UriResolutionContext? = null
    ): Deferred<Result<UriPackageOrWrapper>>
}