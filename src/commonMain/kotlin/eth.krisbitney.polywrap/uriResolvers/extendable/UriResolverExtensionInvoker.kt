package eth.krisbitney.polywrap.uriResolvers.extendable

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import eth.krisbitney.polywrap.core.types.InvokeOptions
import eth.krisbitney.polywrap.core.types.Invoker
import kotlinx.coroutines.Deferred

object UriResolverExtensionInvoker {
    /**
     * Use an invoker to try to resolve a URI using a wrapper that implements the UriResolver interface
     *
     * @param invoker - invokes the wrapper with the resolution URI as an argument
     * @param wrapper - URI for wrapper that implements the UriResolver interface
     * @param uri - the URI to resolve
     */
    suspend fun tryResolveUri(
        invoker: Invoker,
        wrapper: Uri,
        uri: Uri
    ): Deferred<Result<MaybeUriOrManifest>> {
        return invoker.invoke(
            InvokeOptions(
                uri = wrapper,
                method = "tryResolveUri",
                args = msgPackEncode(mapOf("authority" to uri.authority, "path" to uri.path))
            )
        )
    }

    /**
     * Use an invoker to fetch a file using a wrapper that implements the UriResolver interface
     *
     * @param invoker - invokes the wrapper with the filepath as an argument
     * @param wrapper - URI for wrapper that implements the UriResolver interface
     * @param path - a filepath, the format of which depends on the UriResolver
     */
    suspend fun getFile(
        invoker: Invoker,
        wrapper: Uri,
        path: String
    ): Deferred<Result<ByteArray?>> {
        return invoker.invoke(
            InvokeOptions(
                uri = wrapper,
                method = "getFile",
                args = msgPackEncode(mapOf("path" to path))
            )
        )
    }
}
