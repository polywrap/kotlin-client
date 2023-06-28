package io.polywrap.core.resolution

import io.polywrap.core.Invoker
import uniffi.polywrap_native.FfiException
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiUriPackageOrWrapper
import uniffi.polywrap_native.FfiUriResolutionContext
import uniffi.polywrap_native.FfiUriResolver
import kotlin.jvm.Throws

interface UriResolver : FfiUriResolver, AutoCloseable {

    /**
     * Tries to resolve the given [Uri] to a Uri, WrapPackage, or Wrapper.
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [FfiUriResolutionContext] for keeping track of the resolution history.
     * @return A [UriPackageOrWrapper]
     * @throws [FfiException]
     */
    @Throws(FfiException::class)
    override fun tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper
}
