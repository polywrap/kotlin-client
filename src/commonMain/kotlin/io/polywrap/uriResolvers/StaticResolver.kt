package io.polywrap.uriResolvers

import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolver
import uniffi.main.FfiInvoker
import uniffi.main.FfiStaticUriResolver
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriResolutionContext

/**
 * A class that implements [UriResolver] using a map of URI to [FfiUriPackageOrWrapper].
 *
 * @param uriMap A map of URIs to their corresponding [UriPackageOrWrapper]s.
 */
class StaticResolver(uriMap: Map<String, FfiUriPackageOrWrapper>) : UriResolver, AutoCloseable {

    private val ffiResolver = FfiStaticUriResolver(uriMap)

    companion object {

        /**
         * Creates a new [StaticResolver] instance from a list of Pair<Uri, Any> objects.
         *
         * @param staticResolverLikes A list of Pair<Uri, Any> objects to build the [StaticResolver].
         * The [FfiUri] is the URI to resolve, and the [Any] is either a [FfiUri], [WrapPackage], or [Wrapper].
         * @return A new [StaticResolver] instance with the specified URIs and corresponding [FfiUriPackageOrWrapper]s.
         */
        fun from(staticResolverLikes: List<Pair<FfiUri, Any>>): StaticResolver {
            val uriMap = mutableMapOf<String, FfiUriPackageOrWrapper>()
            for (staticResolverLike in staticResolverLikes) {
                val uri = staticResolverLike.first
                when (val item = staticResolverLike.second) {
                    is FfiUri -> {
                        uriMap[uri.toStringUri()] = UriPackageOrWrapper.UriValue(item)
                    }
                    is WrapPackage -> {
                        uriMap[uri.toStringUri()] = UriPackageOrWrapper.UriWrapPackage(uri, item)
                    }
                    is Wrapper -> {
                        uriMap[uri.toStringUri()] = UriPackageOrWrapper.UriWrapper(uri, item)
                    }
                }
            }
            return StaticResolver(uriMap)
        }
    }

    override fun tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper = ffiResolver.tryResolveUri(uri, invoker, resolutionContext)

    override fun close() = ffiResolver.close()
}
