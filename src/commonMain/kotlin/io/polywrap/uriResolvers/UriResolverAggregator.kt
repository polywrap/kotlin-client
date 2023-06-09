package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.resolution.UriResolutionStep
import io.polywrap.core.resolution.UriResolver
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriPackageOrWrapperKind

/**
 * An abstract class for aggregating URI resolvers.
 * Implements the [UriResolver] interface.
 */
abstract class UriResolverAggregator : UriResolver {

    /**
     * Returns a list of URI resolvers for the given URI, client, and resolution context.
     * @param uri The URI being resolved.
     * @param invoker The [Invoker] instance for the current request.
     * @param resolutionContext The [UriResolutionContext] for the current URI resolution process.
     * @return A [Result] containing a list of [UriResolver] instances.
     */
    abstract fun getUriResolvers(
        uri: Uri,
        invoker: FfiInvoker,
        resolutionContext: UriResolutionContext
    ): Result<List<UriResolver>>

    /**
     * Returns the description of the current resolver step.
     * @param uri The URI being resolved.
     * @param result The result of the URI resolution.
     * @return A string representing the step description.
     */
    protected abstract fun getStepDescription(
        uri: Uri,
        result: FfiUriPackageOrWrapper
    ): String

    /**
     * Tries to resolve the given URI with a list of resolvers.
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
        val resolvers = getUriResolvers(uri, invoker, resolutionContext).getOrThrow()
        return tryResolveUriWithResolvers(uri, invoker, resolutionContext, resolvers, false)
    }

    override fun tryResolveUriToPackage(
        uri: Uri,
        invoker: FfiInvoker,
        resolutionContext: UriResolutionContext
    ): FfiUriPackageOrWrapper {
        val resolvers = getUriResolvers(uri, invoker, resolutionContext).getOrThrow()
        return tryResolveUriWithResolvers(uri, invoker, resolutionContext, resolvers, true)
    }

    /**
     * Tries to resolve the given URI with the provided list of resolvers.
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    protected fun tryResolveUriWithResolvers(
        uri: Uri,
        invoker: FfiInvoker,
        resolutionContext: UriResolutionContext,
        resolvers: List<UriResolver>,
        resolveToPackage: Boolean
    ): FfiUriPackageOrWrapper {
        val subContext = resolutionContext.createSubHistoryContext()

        for (resolver in resolvers) {
            val result = when (resolveToPackage) {
                true -> resolver.tryResolveUriToPackage(uri, invoker, subContext)
                false -> resolver.tryResolveUri(uri, invoker, subContext)
            }
            val isUri = result.getKind() == FfiUriPackageOrWrapperKind.URI && result.asUri() == uri
            if (!isUri) {
                resolutionContext.trackStep(
                    UriResolutionStep(
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
                UriResolutionStep(
                    sourceUri = uri,
                    result = it,
                    subHistory = subContext.getHistory(),
                    description = getStepDescription(uri, it)
                )
            )
        }
    }
}
