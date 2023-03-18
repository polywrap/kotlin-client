package eth.krisbitney.polywrap.uriResolvers.embedded

import eth.krisbitney.polywrap.core.resolution.PackageRedirect
import eth.krisbitney.polywrap.core.resolution.UriRedirect
import eth.krisbitney.polywrap.core.resolution.WrapperRedirect

/**
 * A sealed class representing a static resolver.
 */
sealed class StaticResolverLike {

    /**
     * Represents a URI redirect value containing a [UriRedirect] instance.
     *
     * @property redirect The [UriRedirect] instance.
     */
    data class UriRedirectValue(val redirect: UriRedirect) : StaticResolverLike()

    /**
     * Represents a package redirect value containing a [PackageRedirect] instance.
     *
     * @property pkg The [PackageRedirect] instance.
     */
    data class PackageRedirectValue(val pkg: PackageRedirect) : StaticResolverLike()

    /**
     * Represents a wrapper redirect value containing a [WrapperRedirect] instance.
     *
     * @property wrapper The [WrapperRedirect] instance.
     */
    data class WrapperRedirectValue(val wrapper: WrapperRedirect) : StaticResolverLike()
}
