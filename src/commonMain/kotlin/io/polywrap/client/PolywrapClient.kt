package io.polywrap.client

import io.polywrap.core.Client
import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import io.polywrap.core.resolution.Uri
import uniffi.main.FfiClient
import uniffi.main.FfiUriResolutionContext

@OptIn(ExperimentalUnsignedTypes::class)
class PolywrapClient(private val ffiClient: FfiClient) : Invoker(ffiClient.asInvoker()), Client, AutoCloseable {

    override fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: FfiUriResolutionContext?
    ): Result<ByteArray> = runCatching {
        uri.toFfi().use {
            ffiClient.invokeWrapperRaw(
                wrapper = wrapper,
                uri = it,
                method = method,
                args = args?.asUByteArray()?.toList(),
                env = env?.asUByteArray()?.toList(),
                resolutionContext = resolutionContext
            )
        }
    }.map {
        it.toUByteArray().asByteArray()
    }

    override fun loadWrapper(
        uri: Uri,
        resolutionContext: FfiUriResolutionContext?
    ): Result<Wrapper> = runCatching {
        val ffiWrapper = uri.toFfi().use { ffiClient.loadWrapper(it, resolutionContext) }
        Wrapper.fromFfi(ffiWrapper)
    }

    override fun close() {
        ffiInvoker.close()
        ffiClient.close()
    }
}
