package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolver
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriResolutionContext

/**
 * A class that represents a sequential resolver for URIs.
 *
 * @property resolvers A list of [UriResolver] instances to be used sequentially.
 */
class SequentialResolver(private val resolvers: List<UriResolver>) : UriResolverAggregator() {

    override fun getStepDescription(uri: FfiUri, result: UriPackageOrWrapper): String {
        return "SequentialResolver"
    }

    /**
     * Returns a list of URI resolvers to be used sequentially.
     * @param uri The URI being resolved.
     * @param invoker The [Invoker] instance for the current request.
     * @param resolutionContext The [FfiUriResolutionContext] for the current URI resolution process.
     * @return A [Result] containing a list of [UriResolver] instances.
     */
    override fun getUriResolvers(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): Result<List<UriResolver>> {
        return Result.success(resolvers)
    }

    override fun close() = resolvers.forEach { it.close() }
}
