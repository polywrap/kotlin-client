package io.polywrap.uriResolvers

import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolver
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiStaticUriResolver
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriPackageOrWrapper
import uniffi.polywrap_native.FfiUriResolutionContext

/**
 * A class that implements [UriResolver] using a map of URI to [UriPackageOrWrapper].
 *
 * The constructors take ownership of the [UriPackageOrWrapper]s and will close them during construction.
 *
 * @param uriMap A map of URIs to their corresponding [UriPackageOrWrapper]s.
 */
class StaticResolver(uriMap: Map<String, UriPackageOrWrapper>) : UriResolver, AutoCloseable {

    private val ffiResolver = FfiStaticUriResolver(uriMap)

    init { uriMap.values.forEach { it.close() } }

    companion object {

        /**
         * Creates a new [StaticResolver] instance from a list of Pair<[Uri], Any> objects.
         *
         * @param staticResolverLikes A list of Pair<Uri, Any> objects to build the [StaticResolver].
         * The [Uri] is the URI to resolve, and the [Any] is either a [Uri], [WrapPackage], or [Wrapper].
         * @return A new [StaticResolver] instance with the specified URIs and corresponding [FfiUriPackageOrWrapper]s.
         * @throws IllegalArgumentException If any of the [Any] objects are not a [Uri], [WrapPackage], or [Wrapper].
         */
        fun from(staticResolverLikes: List<Pair<Uri, Any>>): StaticResolver {
            val uriMap = mutableMapOf<String, UriPackageOrWrapper>()
            for (staticResolverLike in staticResolverLikes) {
                val uri = staticResolverLike.first
                when (val item = staticResolverLike.second) {
                    is Uri -> {
                        uriMap[uri.toString()] = UriPackageOrWrapper.UriValue(item.toFfi())
                    }
                    is WrapPackage -> {
                        uriMap[uri.toString()] = UriPackageOrWrapper.UriWrapPackage(uri.toFfi(), item)
                    }
                    is Wrapper -> {
                        uriMap[uri.toString()] = UriPackageOrWrapper.UriWrapper(uri.toFfi(), item)
                    }
                    else -> throw IllegalArgumentException("Invalid staticResolverLike: $item")
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
