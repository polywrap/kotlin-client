package eth.krisbitney.polywrap.uriResolvers

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.Client

abstract class UriResolverAggregator : UriResolver {

    abstract suspend fun getUriResolvers(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<List<UriResolver>>

    override suspend fun tryResolveUri(
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

    protected abstract fun getStepDescription(
        uri: Uri,
        result: Result<UriPackageOrWrapper>
    ): String

    protected suspend fun tryResolveUriWithResolvers(
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