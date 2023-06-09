package io.polywrap.uriResolvers

import io.polywrap.core.Invoker
import io.polywrap.core.resolution.UriResolver
import uniffi.polywrap_native.FfiException
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiRecursiveUriResolver
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriPackageOrWrapper
import uniffi.polywrap_native.FfiUriResolutionContext
import kotlin.jvm.Throws

/**
 * A [UriResolver] implementation that resolves URIs recursively.
 *
 * @property resolver The [UriResolver] instance used for resolving URIs.
 */
class RecursiveResolver(private val resolver: UriResolver) : UriResolver, AutoCloseable {

    private val ffiResolver = FfiRecursiveUriResolver(resolver)

    /**
     * Tries to resolve the given [FfiUri] recursively by trying to resolve it again if a redirect to another [FfiUri] occurs.
     *
     * @param uri The [FfiUri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [FfiUriResolutionContext] for keeping track of the resolution history.
     * @return An [FfiUriPackageOrWrapper] if the resolution is successful
     * @throws [FfiException] if resolution fails
     */
    @Throws(FfiException::class)
    override fun tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper = ffiResolver.tryResolveUri(uri, invoker, resolutionContext)

    override fun close() = ffiResolver.close()
}
