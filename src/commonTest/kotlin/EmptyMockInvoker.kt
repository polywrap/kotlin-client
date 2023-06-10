import io.polywrap.core.Invoker
import io.polywrap.core.Wrapper
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import uniffi.main.FfiUri
import uniffi.main.FfiUriResolutionContext

val emptyMockInvoker = object : Invoker() {
    override fun invokeRaw(
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ): Result<ByteArray> {
        throw NotImplementedError()
    }

    override fun invokeRaw(
        uri: FfiUri,
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        resolutionContext: FfiUriResolutionContext?
    ): List<UByte> {
        throw NotImplementedError()
    }

    override fun invokeWrapperRaw(
        wrapper: Wrapper,
        uri: Uri,
        method: String,
        args: ByteArray?,
        env: ByteArray?,
        resolutionContext: UriResolutionContext?
    ): Result<ByteArray> {
        throw NotImplementedError()
    }

    override fun getImplementations(uri: FfiUri): List<FfiUri> {
        throw NotImplementedError()
    }

    override fun getInterfaces(): Map<String, List<FfiUri>>? {
        throw NotImplementedError()
    }

    override fun getEnvByUri(uri: FfiUri): List<UByte>? {
        throw NotImplementedError()
    }
}
