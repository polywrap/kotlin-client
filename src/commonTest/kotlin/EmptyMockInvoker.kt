import io.polywrap.core.Uri
import io.polywrap.core.UriResolutionContext
import io.polywrap.core.types.InvokeOptions
import io.polywrap.core.types.Invoker
import io.polywrap.core.types.Wrapper

val emptyMockInvoker = object : Invoker {
    override fun invokeWrapper(wrapper: Wrapper, options: InvokeOptions): Result<ByteArray> {
        throw NotImplementedError()
    }

    override fun invoke(options: InvokeOptions): Result<ByteArray> {
        throw NotImplementedError()
    }

    override fun getImplementations(
        uri: Uri,
        applyResolution: Boolean,
        resolutionContext: UriResolutionContext?
    ): Result<List<Uri>> {
        throw NotImplementedError()
    }
}
