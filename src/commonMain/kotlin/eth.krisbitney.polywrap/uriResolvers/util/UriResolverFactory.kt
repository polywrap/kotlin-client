package eth.krisbitney.polywrap.uriResolvers.util

import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.uriResolvers.PackageRedirectResolver
import eth.krisbitney.polywrap.uriResolvers.UriRedirectResolver
import eth.krisbitney.polywrap.uriResolvers.WrapperRedirectResolver

/**
 * An object that provides a factory method for creating [UriResolver] instances from various [UriResolverLike] types.
 */
object UriResolverFactory {

    /**
     * Creates a [UriResolver] instance based on the given [UriResolverLike] type.
     *
     * @param resolverLike An instance of [UriResolverLike], which can be one of the following types:
     * - [UriResolverLike.UriResolverValue]
     * - [UriResolverLike.UriRedirectValue]
     * - [UriResolverLike.PackageRedirectValue]
     * - [UriResolverLike.WrapperRedirectValue]
     * @return A [UriResolver] instance based on the input [resolverLike].
     */
    fun from(resolverLike: UriResolverLike): UriResolver {
        return when (resolverLike) {
            is UriResolverLike.UriResolverValue -> resolverLike.resolver
            is UriResolverLike.UriRedirectValue -> UriRedirectResolver(
                resolverLike.redirect.from,
                resolverLike.redirect.to
            )
            is UriResolverLike.PackageRedirectValue -> PackageRedirectResolver(
                resolverLike.pkg.uri,
                resolverLike.pkg.pkg
            )
            is UriResolverLike.WrapperRedirectValue -> WrapperRedirectResolver(
                resolverLike.wrapper.uri,
                resolverLike.wrapper.wrapper
            )
        }
    }
}
