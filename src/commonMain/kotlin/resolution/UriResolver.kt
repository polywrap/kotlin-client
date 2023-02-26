package resolution

import types.Client

/**
 * Defines an entity capable of resolving a wrap URI
 */
interface UriResolver {
  /**
   * Resolve a URI to a wrap package, a wrapper, or a uri
   *
   * @param uri - The URI to resolve
   * @param client - A CoreClient instance that may be used to invoke a wrapper that implements the UriResolver interface
   * @param resolutionContext - The current URI resolution context
   * @return A Promise with a Result containing either a wrap package, a wrapper, or a URI if successful
   */
  suspend fun tryResolveUri(
    uri: Uri,
    client: Client,
    resolutionContext: UriResolutionContext
  ): Result<UriPackageOrWrapper>
}