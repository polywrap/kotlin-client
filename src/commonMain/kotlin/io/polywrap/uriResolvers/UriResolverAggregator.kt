package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolver
import uniffi.polywrap_native.FfiException
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriPackageOrWrapper
import uniffi.polywrap_native.FfiUriPackageOrWrapperKind
import uniffi.polywrap_native.FfiUriResolutionContext
import uniffi.polywrap_native.FfiUriResolutionStep
import kotlin.jvm.Throws

/**
 * An abstract class for aggregating URI resolvers.
 * Implements the [UriResolver] interface.
 */
abstract class UriResolverAggregator : UriResolver {

    /**
     * Returns a list of URI resolvers for the given URI, client, and resolution context.
     * @param uri The URI being resolved.
     * @param invoker The [Invoker] instance for the current request.
     * @param resolutionContext The [FfiUriResolutionContext] for the current URI resolution process.
     * @return A [Result] containing a list of [UriResolver] instances.
     */
    abstract fun getUriResolvers(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): Result<List<UriResolver>>

    /**
     * Returns the description of the current resolver step.
     * @param uri The URI being resolved.
     * @param result The result of the URI resolution.
     * @return A string representing the step description.
     */
    protected abstract fun getStepDescription(
        uri: FfiUri,
        result: FfiUriPackageOrWrapper
    ): String

    /**
     * Tries to resolve the given URI with a list of resolvers.
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
        val resolvers = getUriResolvers(uri, invoker, resolutionContext).getOrThrow()
        return tryResolveUriWithResolvers(uri, invoker, resolutionContext, resolvers)
    }

    /**
     * Tries to resolve the given URI with the provided list of resolvers.
     *
     * @param uri The [FfiUri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [FfiUriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    @Throws(FfiException::class)
    protected fun tryResolveUriWithResolvers(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext,
        resolvers: List<UriResolver>
    ): FfiUriPackageOrWrapper {
        val subContext = resolutionContext.createSubHistoryContext()

        for (resolver in resolvers) {
            val result = resolver.tryResolveUri(uri, invoker, subContext)
            val isUri = result.getKind() == FfiUriPackageOrWrapperKind.URI
            if (!isUri) {
                resolutionContext.trackStep(
                    FfiUriResolutionStep(
                        sourceUri = uri,
                        result = result,
                        subHistory = subContext.getHistory(),
                        description = getStepDescription(uri, result)
                    )
                )

                return result
            }
        }

        return UriPackageOrWrapper.UriValue(uri).also {
            resolutionContext.trackStep(
                FfiUriResolutionStep(
                    sourceUri = uri,
                    result = it,
                    subHistory = subContext.getHistory(),
                    description = getStepDescription(uri, it)
                )
            )
        }
    }
}
