package eth.krisbitney.polywrap.core.types

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext
import kotlinx.coroutines.Deferred

/**
 * Options required for a wrapper invocation.
 *
 * @property uri The Wrapper's URI
 * @property method Method to be executed
 * @property args Arguments for the method, encoded in the MessagePack byte format
 * @property env Env variables for the wrapper invocation, encoded in the MessagePack byte format
 * @property resolutionContext A Uri resolution context
 */
data class InvokeOptions(
    val uri: Uri,
    val method: String,
    val args: ByteArray? = null,
    val env: ByteArray? = null,
    val resolutionContext: UriResolutionContext? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InvokeOptions

        if (uri != other.uri) return false
        if (method != other.method) return false
        if (args != null) {
            if (other.args == null) return false
            if (!args.contentEquals(other.args)) return false
        } else if (other.args != null) return false
        if (env != null) {
            if (other.env == null) return false
            if (!env.contentEquals(other.env)) return false
        } else if (other.env != null) return false
        if (resolutionContext != other.resolutionContext) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + (args?.contentHashCode() ?: 0)
        result = 31 * result + (env?.contentHashCode() ?: 0)
        result = 31 * result + (resolutionContext?.hashCode() ?: 0)
        return result
    }
}

/**
 * Result of a Wrapper invocation.
 *
 * @param TData Type of the invoke result data.
 */
typealias InvokeResult<TData> = Result<TData>

/**
 * An entity capable of invoking wrappers.
 */
interface Invoker {
    /**
     * Invoke a wrapper using an instance of the wrapper.
     *
     * @param wrapper An instance of a Wrapper to invoke.
     * @param options Invoker options to set and a Wrapper instance to invoke.
     * @return A [Deferred] with [Result] containing a MsgPack encoded byte array or an error.
     */
    suspend fun invokeWrapper(wrapper: Wrapper, options: InvokeOptions): Deferred<Result<ByteArray>>

    /**
     * Invoke a wrapper.
     *
     * Unlike [invokeWrapper], this method automatically retrieves and caches the wrapper.
     *
     * @param options Invoker options to set.
     * @return A [Deferred] with [Result] containing a MsgPack encoded byte array or an error.
     */
    suspend fun invoke(options: InvokeOptions): Deferred<Result<ByteArray>>

    /**
     * Returns the interface implementations associated with an interface URI from the
     * configuration used to instantiate the client.
     *
     * @param uri - a wrap URI
     * @param applyResolution - If true, follow redirects to resolve URIs
     * @param resolutionContext - Use and update an existing resolution context
     * @return a Result containing an array of URIs if the request was successful
     */
    suspend fun getImplementations(
        uri: Uri,
        applyResolution: Boolean = false,
        resolutionContext: UriResolutionContext? = null
    ): Deferred<Result<List<Uri>>>
}

/** An invocable entity, such as a wrapper. */
interface Invocable {
    /**
     * Invoke this object.
     *
     * @param options Invoke options to set.
     * @param invoker An [Invoker], capable of invoking this object.
     * @return A [Deferred] with [Result] containing a MsgPack encoded byte array or an error.
     */
    suspend fun invoke(options: InvokeOptions, invoker: Invoker): Deferred<Result<ByteArray>>
}
