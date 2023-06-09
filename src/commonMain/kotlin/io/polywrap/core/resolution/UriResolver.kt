package io.polywrap.core.resolution

import io.polywrap.core.Invoker
import io.polywrap.core.WrapPackage
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriResolutionContext
import uniffi.main.FfiUriResolver

interface UriResolver : FfiUriResolver {

    /**
     * Tries to resolve the given [Uri] to a Uri, WrapPackage, or Wrapper.
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return A [UriPackageOrWrapper]
     * @throws [FfiException]
     */
    override fun tryResolveUri(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper

    /**
     * Tries to resolve the given [Uri] to a Uri, WrapPackage, or Wrapper.
     * Unlike [tryResolveUri], this method prefers to resolve to a [WrapPackage]
     *
     * @param uri The [Uri] to resolve.
     * @param invoker The [Invoker] instance.
     * @param resolutionContext The [UriResolutionContext] for keeping track of the resolution history.
     * @return A [UriPackageOrWrapper]
     * @throws [FfiException]
     */
    fun tryResolveUriToPackage(
        uri: FfiUri,
        invoker: FfiInvoker,
        resolutionContext: FfiUriResolutionContext
    ): FfiUriPackageOrWrapper
}
