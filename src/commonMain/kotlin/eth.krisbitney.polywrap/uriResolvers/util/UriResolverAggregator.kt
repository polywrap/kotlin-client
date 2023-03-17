package eth.krisbitney.polywrap.uriResolvers.util

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.Invoker

abstract class UriResolverAggregator : UriResolver {

    abstract suspend fun getUriResolvers(
        uri: Uri,
        invoker: Invoker,
        resolutionContext: UriResolutionContext
    ): Result<List<UriResolver>>

    override suspend fun tryResolveUri(
        uri: Uri,
        invoker: Invoker,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        val resolverResult = getUriResolvers(uri, invoker, resolutionContext)
        if (resolverResult.isFailure) {
            val exception = resolverResult.exceptionOrNull() ?: Exception("Failed to obtain aggregated URI resolvers")
            return Result.failure(exception)
        }
        val resolvers = resolverResult.getOrThrow()
        return tryResolveUriWithResolvers(uri, invoker, resolvers, resolutionContext)
    }

    protected abstract fun getStepDescription(
        uri: Uri,
        result: Result<UriPackageOrWrapper>
    ): String

    protected suspend fun tryResolveUriWithResolvers(
        uri: Uri,
        invoker: Invoker,
        resolvers: List<UriResolver>,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        val subContext = resolutionContext.createSubHistoryContext()

        for (resolver in resolvers) {
            val result = resolver.tryResolveUri(uri, invoker, subContext)
            val resultVal = result.getOrNull()
            if (!(resultVal is UriPackageOrWrapper.UriValue && resultVal.uri == uri)) {
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