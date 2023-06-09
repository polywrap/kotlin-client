package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.resolution.UriResolver
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiRecursiveUriResolver
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriResolutionContext

/**
 * A [UriResolver] implementation that resolves URIs recursively.
 *
 * @property resolver The [UriResolver] instance used for resolving URIs.
 */
class RecursiveResolver(private val resolver: UriResolver) : UriResolver, AutoCloseable {

    private val ffiResolver = FfiRecursiveUriResolver(resolver)

    /**
     * Tries to resolve the given [Uri] recursively by trying to resolve it again if a redirect to another [Uri] occurs.
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    override fun tryResolveUri(
        uri: Uri,
        invoker: FfiInvoker,
        resolutionContext: UriResolutionContext
    ): FfiUriPackageOrWrapper = ffiResolver.tryResolveUri(uri, invoker, resolutionContext)

    override fun tryResolveUriToPackage(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper = tryResolveUri(uri, invoker, resolutionContext)

    override fun close() = ffiResolver.close()
}
