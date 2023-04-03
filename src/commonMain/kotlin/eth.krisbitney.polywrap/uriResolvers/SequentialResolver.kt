package eth.krisbitney.polywrap.uriResolvers

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.Client

class SequentialResolver(private val resolvers: List<UriResolver>) : UriResolverAggregator() {

    override fun getStepDescription(uri: Uri, result: Result<UriPackageOrWrapper>): String {
        return "SequentialResolver"
    }

    override suspend fun getUriResolvers(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<List<UriResolver>> {
        return Result.success(resolvers)
    }
}
