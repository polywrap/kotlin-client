package io.polywrap.uriResolvers.cache

import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolver
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriPackageOrWrapperKind
import uniffi.main.FfiUriResolutionContext
import uniffi.main.FfiUriResolutionStep
import kotlin.jvm.Throws

/**
 * A URI resolver that uses a cache to store and retrieve the results of resolved URIs.
 *
 * @property resolver The [UriResolver] to use when resolving URIs.
 * @property cache The cache to store and retrieve resolved URIs.
 */
class WrapperCacheResolver(
    private val resolver: UriResolver,
    private val cache: WrapperCache
) : UriResolver, AutoCloseable {

    /**
     * Tries to resolve the given URI using a cache and returns the result.
     *
     * @param uri The [FfiUri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [FfiUriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    @Throws(FfiException::class)
    override fun tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper {
        val wrapper = cache.get(uri.toStringUri())

        // return from cache if available
        if (wrapper != null) {
            val result = UriPackageOrWrapper.UriWrapper(uri, wrapper)
            resolutionContext.trackStep(
                FfiUriResolutionStep(
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
            FfiUriResolutionStep(
                sourceUri = uri,
                result = finalResult,
                subHistory = subContext.getHistory(),
                description = "CacheResolver"
            )
        )

        return finalResult
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
        subContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper {
        return when (uriPackageOrWrapper.getKind()) {
            FfiUriPackageOrWrapperKind.URI -> uriPackageOrWrapper

            FfiUriPackageOrWrapperKind.PACKAGE -> {
                val uriWrapPackage = uriPackageOrWrapper.asPackage()
                val resolvedUri = uriWrapPackage.getUri()
                val wrapPackage = uriWrapPackage.getPackage()
                val wrapper = wrapPackage.createWrapper()

                val resolutionPath = subContext.getResolutionPath()
                for (uri: FfiUri in resolutionPath) {
                    cache.set(uri.toStringUri(), wrapper as Wrapper)
                }

                UriPackageOrWrapper.UriWrapper(resolvedUri, wrapper as Wrapper)
            }

            FfiUriPackageOrWrapperKind.WRAPPER -> {
                val wrapper = uriPackageOrWrapper.asWrapper()

                val resolutionPath = subContext.getResolutionPath()
                for (uri: FfiUri in resolutionPath) {
                    cache.set(uri.toStringUri(), wrapper.getWrapper() as Wrapper)
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
