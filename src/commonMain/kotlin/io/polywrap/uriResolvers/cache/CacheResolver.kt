package io.polywrap.uriResolvers.cache

import io.polywrap.core.resolution.*
import io.polywrap.core.types.*

/**
 * A URI resolver that uses a cache to store and retrieve the results of resolved URIs.
 *
 * @property resolver The [UriResolver] to use when resolving URIs.
 * @property cache The cache to store and retrieve resolved URIs.
 */
class CacheResolver(
    private val resolver: UriResolver,
    private val cache: WrapperCache
) : UriResolver {

    /**
     * Tries to resolve the given URI using a cache and returns the result.
     *
     * @param uri The URI to resolve.
     * @param client The invoker of the resolution.
     * @param resolutionContext The context for the resolution.
     * @param resolveToPackage If `true`, the resolver will resolve the URI to a [WrapPackage] instead of a [Wrapper].
     * @return A [Result] containing the resolved [UriPackageOrWrapper] on success, or an exception on failure.
     */
    override fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        if (resolveToPackage) {
            val subContext = resolutionContext.createSubHistoryContext()
            return resolver.tryResolveUri(uri, client, subContext, resolveToPackage)
        }

        val wrapper = cache.get(uri)

        // return from cache if available
        if (wrapper != null) {
            val result = Result.success(UriPackageOrWrapper.WrapperValue(uri, wrapper))
            resolutionContext.trackStep(
                UriResolutionStep(
                    sourceUri = uri,
                    result = result,
                    description = "SynchronizedCacheResolver (Cache)"
                )
            )
            return result
        }

        // resolve uri if not in cache
        val subContext = resolutionContext.createSubHistoryContext()
        val result = resolver.tryResolveUri(uri, client, subContext, resolveToPackage)

        val finalResult = if (result.isSuccess) {
            cacheResult(result.getOrThrow(), subContext)
        } else {
            result
        }

        resolutionContext.trackStep(
            UriResolutionStep(
                sourceUri = uri,
                result = finalResult,
                subHistory = subContext.getHistory(),
                description = "SynchronizedCacheResolver"
            )
        )

        return finalResult
    }

    /**
     * Caches the result of a resolved URI based on its type.
     *
     * @param uriPackageOrWrapper The resolved URI to cache.
     * @param subContext The context for the resolution.
     * @return A [Result] containing the cached [UriPackageOrWrapper] on success, or an exception on failure.
     */
    private fun cacheResult(
        uriPackageOrWrapper: UriPackageOrWrapper,
        subContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        return when (uriPackageOrWrapper) {
            is UriPackageOrWrapper.UriValue -> Result.success(uriPackageOrWrapper)

            is UriPackageOrWrapper.PackageValue -> {
                val resolvedUri = uriPackageOrWrapper.uri
                val wrapPackage = uriPackageOrWrapper.pkg
                val createResult = wrapPackage.createWrapper()

                if (createResult.isFailure) {
                    Result.failure(createResult.exceptionOrNull()!!)
                } else {
                    val wrapper = createResult.getOrThrow()
                    val resolutionPath = subContext.getResolutionPath()
                    for (uri: Uri in resolutionPath) {
                        cache.set(uri, wrapper)
                    }

                    Result.success(UriPackageOrWrapper.WrapperValue(resolvedUri, wrapper))
                }
            }

            is UriPackageOrWrapper.WrapperValue -> {
                val wrapper = uriPackageOrWrapper.wrapper
                val resolutionPath = subContext.getResolutionPath()
                for (uri: Uri in resolutionPath) {
                    cache.set(uri, wrapper)
                }

                Result.success(uriPackageOrWrapper)
            }
        }
    }
}
