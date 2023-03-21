package eth.krisbitney.polywrap.uriResolvers.extendable

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriPackageOrWrapper
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import eth.krisbitney.polywrap.core.types.Client
import eth.krisbitney.polywrap.uriResolvers.UriResolverAggregator

class ExtendableUriResolver(
    private val extInterfaceUris: List<Uri> = defaultExtInterfaceUris,
) : UriResolverAggregator() {
    private val resolverName = "ExtendableUriResolver"
    companion object {
        val defaultExtInterfaceUris: List<Uri> = listOf(
            Uri("wrap://ens/wraps.eth:uri-resolver-ext@1.1.0"),
            Uri("wrap://ens/wraps.eth:uri-resolver-ext@1.0.0")
        )
    }

    override suspend fun getUriResolvers(
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
            ).await()

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

    override suspend fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        val result = getUriResolvers(uri, client, resolutionContext)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        val resolvers = result.getOrThrow()

        if (resolvers.isEmpty()) {
            return Result.success(UriPackageOrWrapper.UriValue(uri))
        }

        return super.tryResolveUriWithResolvers(uri, client, resolvers, resolutionContext)
    }

    override fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String = resolverName
}
