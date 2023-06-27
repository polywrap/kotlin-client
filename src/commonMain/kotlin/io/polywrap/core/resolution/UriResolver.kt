package io.polywrap.core.resolution

import io.polywrap.core.Invoker
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapper
import uniffi.main.FfiUriResolutionContext
import uniffi.main.FfiUriResolver
import kotlin.jvm.Throws

interface UriResolver : FfiUriResolver {

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
