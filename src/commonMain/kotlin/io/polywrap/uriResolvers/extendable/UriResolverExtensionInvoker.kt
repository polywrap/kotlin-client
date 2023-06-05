package io.polywrap.uriResolvers.extendable

import io.polywrap.core.Uri
import io.polywrap.core.types.InvokeOptions
import io.polywrap.core.types.Invoker
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import kotlinx.serialization.serializer

object UriResolverExtensionInvoker {
    /**
     * Use an invoker to try to resolve a URI using a wrapper that implements the UriResolver interface
     *
     * @param invoker - invokes the wrapper with the resolution URI as an argument
     * @param wrapper - URI for wrapper that implements the UriResolver interface
     * @param uri - the URI to resolve
     */
    fun tryResolveUri(
        invoker: Invoker,
        wrapper: Uri,
        uri: Uri
    ): Result<MaybeUriOrManifest> {
        val result = invoker.invoke(
            InvokeOptions(
                uri = wrapper,
                method = "tryResolveUri",
                args = msgPackEncode(serializer(), mapOf("authority" to uri.authority, "path" to uri.path))
            )
        )
        return if (result.isFailure) {
            Result.failure(result.exceptionOrNull()!!)
        } else {
            msgPackDecode(MaybeUriOrManifest.serializer(), result.getOrThrow())
        }
    }

    /**
     * Use an invoker to fetch a file using a wrapper that implements the UriResolver interface
     *
     * @param invoker - invokes the wrapper with the filepath as an argument
     * @param wrapper - URI for wrapper that implements the UriResolver interface
     * @param path - a filepath, the format of which depends on the UriResolver
     */
    fun getFile(
        invoker: Invoker,
        wrapper: Uri,
        path: String
    ): Result<ByteArray?> {
        val result = invoker.invoke(
            InvokeOptions(
                uri = wrapper,
                method = "getFile",
                args = msgPackEncode(serializer(), mapOf("path" to path))
            )
        )
        return if (result.isFailure) {
            Result.failure(result.exceptionOrNull()!!)
        } else {
            msgPackDecode(serializer(), result.getOrThrow())
        }
    }
}
