package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.resolution.UriResolver
import uniffi.main.FfiException
import uniffi.main.FfiExtendableUriResolver
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriResolutionContext

/**
 * A URI resolver class that aggregates multiple URI resolvers from Polywrap wrappers implementing the
 * URI Resolver Extension wrapper interface.
 */
class ExtendableUriResolver() : UriResolver, AutoCloseable {

    private val ffiResolver = FfiExtendableUriResolver("ExtendableUriResolver")

    /**
     * Attempts to resolve the given URI using the extension URI resolvers.
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    override fun tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper = ffiResolver.tryResolveUri(uri, invoker, resolutionContext)

    override fun tryResolveUriToPackage(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper = tryResolveUri(uri, invoker, resolutionContext)

    override fun close() = ffiResolver.close()
}
