package io.polywrap.core.resolution

import io.polywrap.core.types.Client

/**
 * Defines an entity capable of resolving a wrap URI
 */
interface UriResolver {
    /**
     * Resolve a URI to a wrap package, a wrapper, or a uri
     *
     * @param uri - The URI to resolve
     * @param client - An Invoker instance that may be used to invoke a wrapper that implements the UriResolver interface
     * @param resolutionContext - The current URI resolution context
     * @param resolveToPackage - If true, the resolver will attempt to resolve the URI to a wrap package.
     * If false, the resolver will attempt to resolve the URI to a wrapper. There is no guarantee that the result will
     * contain a package or wrapper, even if this parameter is true.
     * @return A Promise with a Result containing either a wrap package, a wrapper, or a URI if successful
     */
    suspend fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean = false
    ): Result<UriPackageOrWrapper>
}
