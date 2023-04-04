import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriResolutionContext
import io.polywrap.core.types.InvokeOptions
import io.polywrap.core.types.Invoker
import io.polywrap.core.types.Wrapper
import kotlinx.coroutines.Deferred

val emptyMockInvoker = object : Invoker {
    override suspend fun invokeWrapper(wrapper: Wrapper, options: InvokeOptions): Deferred<Result<ByteArray>> {
        throw NotImplementedError()
    }

    override suspend fun invoke(options: InvokeOptions): Deferred<Result<ByteArray>> {
        throw NotImplementedError()
    }

    override suspend fun getImplementations(
        uri: Uri,
        applyResolution: Boolean,
        resolutionContext: UriResolutionContext?
    ): Deferred<Result<List<Uri>>> {
        throw NotImplementedError()
    }
}
