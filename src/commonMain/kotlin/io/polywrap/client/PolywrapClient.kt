package io.polywrap.client

import io.polywrap.core.Client
import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import uniffi.main.FfiClient

@OptIn(ExperimentalUnsignedTypes::class)
class PolywrapClient(val ffiClient: FfiClient) : Invoker(ffiClient.asInvoker()), Client, AutoCloseable {

    override fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ): Result<ByteArray> = runCatching {
        ffiClient.invokeWrapperRaw(
            wrapper = wrapper,
            uri = uri,
            method = method,
            args = args?.asUByteArray()?.toList(),
            env = env?.asUByteArray()?.toList(),
            resolutionContext = resolutionContext
        )
    }.map {
        it.toUByteArray().asByteArray()
    }

    override fun loadWrapper(
        uri: Uri,
        resolutionContext: UriResolutionContext?
    ): Result<Wrapper> = runCatching {
        val ffiWrapper = ffiClient.loadWrapper(uri, resolutionContext)
        Wrapper.fromFfi(ffiWrapper)
    }

    override fun close() = ffiClient.close()
}
