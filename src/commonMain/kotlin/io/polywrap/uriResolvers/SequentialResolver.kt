package io.polywrap.uriResolvers

import io.polywrap.core.Uri
import io.polywrap.core.UriResolutionContext
import io.polywrap.core.resolution.*
import io.polywrap.core.types.Client

/**
 * A class that represents a sequential resolver for URIs.
 *
 * @property resolvers A list of [UriResolver] instances to be used sequentially.
 */
class SequentialResolver(private val resolvers: List<UriResolver>) : UriResolverAggregator() {

    /**
     * Returns the description of the current resolution step.
     * @param uri The URI being resolved.
     * @param result The result of the URI resolution as a [Result] of [UriPackageOrWrapper].
     * @return A string representing the step description.
     */
    override fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String {
        return "SequentialResolver"
    }

    /**
     * Returns a list of URI resolvers to be used sequentially.
     * @param uri The URI being resolved.
     * @param client The [Client] instance for the current request.
     * @param resolutionContext The [UriResolutionContext] for the current URI resolution process.
     * @return A [Result] containing a list of [UriResolver] instances.
     */
    override fun getUriResolvers(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<List<UriResolver>> {
        return Result.success(resolvers)
    }
}
