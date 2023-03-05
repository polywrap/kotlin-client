package eth.krisbitney.polywrap.core.types

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.resolution.UriResolutionContext

/**
 * Options required for a wrapper invocation.
 *
 * @property uri The Wrapper's URI
 * @property method Method to be executed
 * @property args Arguments for the method, encoded in the Msgpack byte format
 * @property env Env variables for the wrapper invocation
 * @property resolutionContext A Uri resolution context
 */
data class InvokeOptions(
    val uri: Uri,
    val method: String,
    val args: ByteArray? = null,
    val env: Map<String, Any>? = null, // TODO - should this be JSON?
    val resolutionContext: UriResolutionContext? = null,
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
        if (env != other.env) return false
        if (resolutionContext != other.resolutionContext) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + (args?.contentHashCode() ?: 0)
        result = 31 * result + (env?.hashCode() ?: 0)
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
     * @param TData Type of the invoke result data.
     * @return A Promise with a Result containing the return value or an error.
     */
    suspend fun <TData>invokeWrapper(
        wrapper: Wrapper,
        options: InvokeOptions,
    ): InvokeResult<TData>

    /**
     * Invoke a wrapper.
     *
     * Unlike [invokeWrapper], this method automatically retrieves and caches the wrapper.
     *
     * @param options Invoker options to set.
     * @param TData Type of the invoke result data.
     * @return A Promise with a Result containing the return value or an error.
     */
    suspend fun <TData>invoke(options: InvokeOptions): InvokeResult<TData>
}

/**
 * Result of a Wrapper invocation, possibly Msgpack-encoded.
 *
 * @param TData Type of the invoke result data.
 * @property result The result of the invocation.
 * @property encoded If true, result (if successful) contains a Msgpack-encoded byte array.
 */
data class InvocableResult<TData>(
    val result: InvokeResult<TData>,
    val encoded: Boolean = false
)

/** An invocable entity, such as a wrapper. */
interface Invocable {
    /**
     * Invoke this object.
     *
     * @param options Invoke options to set.
     * @param invoker An [Invoker], capable of invoking this object.
     * @return A Promise with a [InvocableResult] containing the return value or an error.
     */
    suspend fun <TResult>invoke(
        options: InvokeOptions,
        invoker: Invoker
    ): InvocableResult<TResult>
}