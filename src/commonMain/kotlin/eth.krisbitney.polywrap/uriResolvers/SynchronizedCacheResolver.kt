package eth.krisbitney.polywrap.uriResolvers

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.uriResolvers.cache.WrapperCache
import eth.krisbitney.polywrap.uriResolvers.util.UriResolverFactory
import eth.krisbitney.polywrap.uriResolvers.util.UriResolverLike
import kotlinx.coroutines.sync.Mutex

/**
 * A URI resolver that uses a synchronized cache to store and retrieve the results of resolved URIs.
 *
 * @property resolverToCache The [UriResolver] to use when resolving URIs.
 * @property cache The cache to store and retrieve resolved URIs.
 */
class SynchronizedCacheResolver(
    private val resolverToCache: UriResolver,
    private val cache: WrapperCache
) : UriResolver {

    private val locks = mutableMapOf<Uri, Mutex>()

    companion object {
        /**
         * Creates a [SynchronizedCacheResolver] instance with the given resolver and cache.
         *
         * @param resolver The resolver to use for creating the SynchronizedCacheResolver instance.
         * @param cache The cache to use for storing and retrieving resolved URIs.
         * @return A new SynchronizedCacheResolver instance.
         */
        fun from(resolver: UriResolverLike, cache: WrapperCache): SynchronizedCacheResolver {
            return SynchronizedCacheResolver(UriResolverFactory.from(resolver), cache)
        }
    }

    /**
     * Checks if the given URI is cached.
     *
     * @param uri The URI to check.
     * @return `true` if the URI is cached, `false` otherwise.
     */
    fun isCached(uri: Uri): Boolean {
        return cache.get(uri) != null
    }

    /**
     * Checks if the given URI is locked.
     *
     * @param uri The URI to check.
     * @return `true` if the URI is locked, `false` otherwise.
     */
    fun isLocked(uri: Uri): Boolean {
        return locks.containsKey(uri)
    }

    /**
     * Tries to resolve the given URI using a cache and returns the result.
     *
     * @param uri The URI to resolve.
     * @param invoker The invoker of the resolution.
     * @param resolutionContext The context for the resolution.
     * @return A [Result] containing the resolved [UriPackageOrWrapper] on success, or an exception on failure.
     */
    override suspend fun tryResolveUri(
        uri: Uri,
        invoker: Invoker,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        acquireMutex(uri)

        val wrapper = cache.get(uri)

        // return from cache if available
        if (wrapper != null) {
            val result = Result.success(UriPackageOrWrapper.WrapperValue(WrapperRedirect(uri, wrapper)))
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

        val result = resolverToCache.tryResolveUri(uri, invoker, subContext)

        val finalResult = if (result.isSuccess) {
            val cachedResult = cacheResult(result.getOrThrow(), subContext)
            cancelMutex(uri)
            cachedResult
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
    private suspend fun cacheResult(
        uriPackageOrWrapper: UriPackageOrWrapper,
        subContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        return when (uriPackageOrWrapper) {
            is UriPackageOrWrapper.UriValue -> Result.success(uriPackageOrWrapper)

            is UriPackageOrWrapper.PackageValue -> {
                val resolvedUri = uriPackageOrWrapper.pkg.uri
                val wrapPackage = uriPackageOrWrapper.pkg.pkg
                val createResult = wrapPackage.createWrapper()

                if (createResult.isFailure) {
                    Result.failure(createResult.exceptionOrNull() ?: Exception("Failed to create wrapper"))
                } else {
                    val wrapper = createResult.getOrThrow()
                    val resolutionPath = subContext.getResolutionPath()
                    for (uri: Uri in resolutionPath) {
                        cache.set(uri, wrapper)
                    }

                    Result.success(UriPackageOrWrapper.WrapperValue(WrapperRedirect(resolvedUri, wrapper)))
                }
            }

            is UriPackageOrWrapper.WrapperValue -> {
                val wrapper = uriPackageOrWrapper.wrapper.wrapper
                val resolutionPath = subContext.getResolutionPath()
                for (uri: Uri in resolutionPath) {
                    cache.set(uri, wrapper)
                }

                Result.success(uriPackageOrWrapper)
            }
        }
    }

    /**
     * Acquires a mutex lock for the given URI if it's not cached.
     *
     * @param uri The URI for which to acquire a lock.
     */
    private suspend fun acquireMutex(uri: Uri) {
        if (cache.get(uri) == null) {
            val lock = locks.getOrPut(uri) { Mutex() }
            lock.lock()
        }
    }

    /**
     * Releases the mutex lock for the given URI if it's locked.
     *
     * @param uri The URI for which to release the lock.
     */
    private fun cancelMutex(uri: Uri) {
        val lock = locks[uri]
        if (lock != null) {
            lock.unlock()
            locks.remove(uri)
        }
    }
}