package io.polywrap.uriResolvers.cache

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.resolution.UriResolutionStep
import io.polywrap.core.resolution.UriResolver
import io.polywrap.core.resolution.UriWrapper
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriPackageOrWrapperKind
import uniffi.main.FfiUriResolutionContext

/**
 * A URI resolver that uses a cache to store and retrieve the results of resolved URIs.
 *
 * @property resolver The [UriResolver] to use when resolving URIs.
 * @property cache The cache to store and retrieve resolved URIs.
 */
class CacheResolver(
    private val resolver: UriResolver,
    private val cache: WrapperCache
) : UriResolver, AutoCloseable {

    /**
     * Tries to resolve the given URI using a cache and returns the result.
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    override fun tryResolveUri(
        uri: Uri,
        invoker: FfiInvoker,
        resolutionContext: UriResolutionContext
    ): FfiUriPackageOrWrapper {
        val wrapper = cache.get(uri.toStringUri())

        // return from cache if available
        if (wrapper != null) {
            val result = UriWrapper(uri, wrapper)
            resolutionContext.trackStep(
                UriResolutionStep(
                    sourceUri = uri,
                    result = result,
                    description = "CacheResolver (Cache)",
                    subHistory = null
                )
            )
            return result
        }

        // resolve uri if not in cache
        val subContext = resolutionContext.createSubHistoryContext()
        val result = resolver.tryResolveUri(uri, invoker, subContext)

        val finalResult = cacheResult(result, subContext)

        resolutionContext.trackStep(
            UriResolutionStep(
                sourceUri = uri,
                result = finalResult,
                subHistory = subContext.getHistory(),
                description = "CacheResolver"
            )
        )

        return finalResult
    }

    override fun tryResolveUriToPackage(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper {
        val subContext = resolutionContext.createSubHistoryContext()
        val result = resolver.tryResolveUriToPackage(uri, invoker, subContext)
        resolutionContext.trackStep(
            UriResolutionStep(
                sourceUri = uri,
                result = result,
                subHistory = subContext.getHistory(),
                description = "CacheResolver"
            )
        )
        return result
    }

    /**
     * Caches the result of a resolved URI based on its type.
     *
     * @param uriPackageOrWrapper The resolved URI to cache.
     * @param subContext The context for the resolution.
     * @return An [FfiUriPackageOrWrapper]
     */
    private fun cacheResult(
        uriPackageOrWrapper: FfiUriPackageOrWrapper,
        subContext: UriResolutionContext
    ): FfiUriPackageOrWrapper {
        return when (uriPackageOrWrapper.getKind()) {
            FfiUriPackageOrWrapperKind.URI -> uriPackageOrWrapper

            FfiUriPackageOrWrapperKind.PACKAGE -> {
                val uriWrapPackage = uriPackageOrWrapper.asPackage()
                val resolvedUri = uriWrapPackage.getUri()
                val wrapPackage = uriWrapPackage.getPackage()
                val wrapper = wrapPackage.createWrapper()

                val resolutionPath = subContext.getResolutionPath()
                for (uri: Uri in resolutionPath) {
                    cache.set(uri.toStringUri(), wrapper)
                }

                UriWrapper(resolvedUri, wrapper)
            }

            FfiUriPackageOrWrapperKind.WRAPPER -> {
                val wrapper = uriPackageOrWrapper.asWrapper()

                val resolutionPath = subContext.getResolutionPath()
                for (uri: Uri in resolutionPath) {
                    cache.set(uri.toStringUri(), wrapper.getWrapper())
                }

                uriPackageOrWrapper
            }
        }
    }

    override fun close() {
        if (cache is AutoCloseable) {
            cache.close()
        }
    }
}
