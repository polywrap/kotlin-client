package io.polywrap.uriResolvers

import io.polywrap.core.resolution.*
import io.polywrap.core.types.Client

/**
 * An abstract class for aggregating URI resolvers.
 * Implements the [UriResolver] interface.
 */
abstract class UriResolverAggregator : UriResolver {

    /**
     * Returns a list of URI resolvers for the given URI, client, and resolution context.
     * @param uri The URI being resolved.
     * @param client The [Client] instance for the current request.
     * @param resolutionContext The [UriResolutionContext] for the current URI resolution process.
     * @return A [Result] containing a list of [UriResolver] instances.
     */
    abstract fun getUriResolvers(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<List<UriResolver>>

    /**
     * Tries to resolve the given URI with a list of resolvers.
     * @param uri The URI being resolved.
     * @param client The [Client] instance for the current request.
     * @param resolutionContext The [UriResolutionContext] for the current URI resolution process.
     * @param resolveToPackage A flag indicating whether the URI should be resolved to a package or not.
     * @return A [Result] containing a [UriPackageOrWrapper] instance.
     */
    override fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        val resolverResult = getUriResolvers(uri, client, resolutionContext)
        if (resolverResult.isFailure) {
            return Result.failure(resolverResult.exceptionOrNull()!!)
        }
        val resolvers = resolverResult.getOrThrow()
        return tryResolveUriWithResolvers(uri, client, resolvers, resolutionContext, resolveToPackage)
    }

    /**
     * Returns the description of the current resolver step.
     * @param uri The URI being resolved.
     * @param result The result of the URI resolution as a [Result] of [UriPackageOrWrapper].
     * @return A string representing the step description.
     */
    protected abstract fun getStepDescription(
        uri: Uri,
        result: Result<UriPackageOrWrapper>
    ): String

    /**
     * Tries to resolve the given URI with the provided list of resolvers.
     * @param uri The URI being resolved.
     * @param client The [Client] instance for the current request.
     * @param resolvers A list of [UriResolver] instances to use for resolving the URI.
     * @param resolutionContext The [UriResolutionContext] for the current URI resolution process.
     * @param resolveToPackage A flag indicating whether the URI should be resolved to a package or not.
     * @return A [Result] containing a [UriPackageOrWrapper] instance.
     */
    protected fun tryResolveUriWithResolvers(
        uri: Uri,
        client: Client,
        resolvers: List<UriResolver>,
        resolutionContext: UriResolutionContext,
        resolveToPackage: Boolean
    ): Result<UriPackageOrWrapper> {
        val subContext = resolutionContext.createSubHistoryContext()

        for (resolver in resolvers) {
            val result = resolver.tryResolveUri(uri, client, subContext, resolveToPackage)
            val resultVal = result.getOrNull()
            val isUri = resultVal is UriPackageOrWrapper.UriValue && resultVal.uri == uri
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

        val result = Result.success(UriPackageOrWrapper.UriValue(uri))

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
