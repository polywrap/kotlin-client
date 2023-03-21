package eth.krisbitney.polywrap.uriResolvers.embedded

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.Client

/**
 * A class that implements [UriResolver] using a predefined map of URIs to [UriPackageOrWrapper]s.
 *
 * @property uriMap A map of URIs to their corresponding [UriPackageOrWrapper]s.
 */
class StaticResolver(val uriMap: Map<String, UriPackageOrWrapper>) : UriResolver {

    companion object {

        /**
         * Creates a new [StaticResolver] instance from a list of [StaticResolverLike] objects.
         *
         * @param staticResolverLikes A list of [StaticResolverLike] objects to build the [StaticResolver].
         * @return A new [StaticResolver] instance with the specified URIs and corresponding [UriPackageOrWrapper]s.
         */
        fun from(staticResolverLikes: List<StaticResolverLike>): StaticResolver {
            val uriMap = mutableMapOf<String, UriPackageOrWrapper>()
            for (staticResolverLike in staticResolverLikes) {
                when (staticResolverLike) {
                    is StaticResolverLike.UriRedirectValue -> {
                        val from = staticResolverLike.redirect.from
                        val to = staticResolverLike.redirect.to
                        uriMap[from.uri] = UriPackageOrWrapper.UriValue(to)
                    }

                    is StaticResolverLike.PackageRedirectValue -> {
                        val uri = staticResolverLike.pkg.uri
                        val pkg = staticResolverLike.pkg.pkg
                        uriMap[uri.uri] = UriPackageOrWrapper.PackageValue(uri, pkg)
                    }

                    is StaticResolverLike.WrapperRedirectValue -> {
                        val uri = staticResolverLike.wrapper.uri
                        val wrapper = staticResolverLike.wrapper.wrapper
                        uriMap[uri.uri] = UriPackageOrWrapper.WrapperValue(uri, wrapper)
                    }
                }
            }
            return StaticResolver(uriMap)
        }
    }

    /**
     * Tries to resolve the given [uri] using the predefined [uriMap].
     *
     * @param uri The URI to resolve.
     * @param client The [Client] instance used to invoke a wrapper implementing the [UriResolver] interface.
     * @param resolutionContext The current URI resolution context.
     * @return A [Result] containing a wrap package, a wrapper, or a URI if successful.
     */
    override suspend fun tryResolveUri(
        uri: Uri,
        client: Client,
        resolutionContext: UriResolutionContext
    ): Result<UriPackageOrWrapper> {
        val uriPackageOrWrapper = uriMap[uri.uri]
        val result: Result<UriPackageOrWrapper>
        val description: String

        if (uriPackageOrWrapper != null) {
            result = Result.success(uriPackageOrWrapper)
            description = when (uriPackageOrWrapper) {
                is UriPackageOrWrapper.UriValue -> {
                    "StaticResolver - Redirect (${uri.uri} - ${uriPackageOrWrapper.uri.uri})"
                }

                is UriPackageOrWrapper.PackageValue -> {
                    "StaticResolver - Package (${uri.uri})"
                }

                is UriPackageOrWrapper.WrapperValue -> {
                    "StaticResolver - Wrapper (${uri.uri})"
                }
            }
        } else {
            result = Result.success(UriPackageOrWrapper.UriValue(uri))
            description = "StaticResolver - Miss"
        }

        resolutionContext.trackStep(
            UriResolutionStep(
                sourceUri = uri,
                result = result,
                description = description
            )
        )

        return result
    }
}
