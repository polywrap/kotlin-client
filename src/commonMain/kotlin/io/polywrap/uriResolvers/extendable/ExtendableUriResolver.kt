package io.polywrap.uriResolvers.extendable

import io.polywrap.core.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.UriResolutionContext
import io.polywrap.core.types.Client
import io.polywrap.uriResolvers.UriResolverAggregator

/**
 * A URI resolver class that aggregates multiple URI resolvers from Polywrap wrappers implementing the
 * URI Resolver Extension wrapper interface. Inherits from [UriResolverAggregator].
 *
 * @property extInterfaceUris A list of URIs that resolve to the extension wrappers implementing the
 * URI Resolver Extension interface. Defaults to [defaultExtInterfaceUris].
 */
class ExtendableUriResolver(
    private val extInterfaceUris: List<Uri> = defaultExtInterfaceUris
) : UriResolverAggregator() {

    /**
     * Companion object containing the default URI Resolver Extension interface URIs.
     */
    companion object {
        val defaultExtInterfaceUris: List<Uri> = listOf(
            Uri("wrap://ens/wraps.eth:uri-resolver-ext@1.1.0"),
            Uri("wrap://ens/wraps.eth:uri-resolver-ext@1.0.0")
        )
    }

    /**
     * Returns the description of the current resolver step.
     * @param uri The URI being resolved.
     * @param result The result of the URI resolution as a [Result] of [UriPackageOrWrapper].
     * @return A string representing the step description.
     */
    override fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String {
        return "ExtendableUriResolver"
    }

    /**
     * Uses the client to retrieve a list of [UriResolverWrapper] instances to resolve the given URI.
     * @param uri The URI being resolved.
     * @param client The [Client] instance for the current request.
     * @param resolutionContext The [UriResolutionContext] for the current URI resolution process.
     * @return A [Result] containing a list of [UriResolverWrapper] instances.
     */
    override fun getUriResolvers(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<List<UriResolverWrapper>> {
        val uriResolverImpls = mutableListOf<Uri>()

        for (extInterfaceUri in extInterfaceUris) {
            val getImplementationsResult = client.getImplementations(
                extInterfaceUri,
                false,
                resolutionContext.createSubContext()
            )

            if (!getImplementationsResult.isSuccess) {
                return Result.failure(getImplementationsResult.exceptionOrNull()!!)
            }

            uriResolverImpls.addAll(getImplementationsResult.getOrThrow())
        }

        val resolvers = uriResolverImpls
            .filter { !resolutionContext.isResolving(it) }
            .map { UriResolverWrapper(it) }

        return Result.success(resolvers)
    }

    /**
     * Attempts to resolve the given URI using the extension URI resolvers.
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
        val result = getUriResolvers(uri, client, resolutionContext)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        val resolvers = result.getOrThrow()

        if (resolvers.isEmpty()) {
            return Result.success(UriPackageOrWrapper.UriValue(uri))
        }

        return super.tryResolveUriWithResolvers(uri, client, resolvers, resolutionContext, resolveToPackage)
    }
}
