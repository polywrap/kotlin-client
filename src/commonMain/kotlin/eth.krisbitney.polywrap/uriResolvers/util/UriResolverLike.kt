package eth.krisbitney.polywrap.uriResolvers.util

import eth.krisbitney.polywrap.core.resolution.PackageRedirect
import eth.krisbitney.polywrap.core.resolution.UriRedirect
import eth.krisbitney.polywrap.core.resolution.UriResolver
import eth.krisbitney.polywrap.core.resolution.WrapperRedirect

/**
 * Represents a sealed class for different types of URI resolvers.
 */
sealed class UriResolverLike {

    /**
     * Represents a URI resolver value containing a [UriResolver] instance.
     *
     * @property resolver The [UriResolver] instance.
     */
    data class UriResolverValue(val resolver: UriResolver) : UriResolverLike()

    /**
     * Represents a URI redirect value containing a [UriRedirect] instance.
     *
     * @property redirect The [UriRedirect] instance.
     */
    data class UriRedirectValue(val redirect: UriRedirect) : UriResolverLike()

    /**
     * Represents a package redirect value containing a [PackageRedirect] instance.
     *
     * @property pkg The [PackageRedirect] instance.
     */
    data class PackageRedirectValue(val pkg: PackageRedirect) : UriResolverLike()
    /**
     * Represents a wrapper redirect value containing a [WrapperRedirect] instance.
     *
     * @property wrapper The [WrapperRedirect] instance.
     */
    data class WrapperRedirectValue(val wrapper: WrapperRedirect) : UriResolverLike()
}
