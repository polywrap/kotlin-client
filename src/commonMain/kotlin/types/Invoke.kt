package types

import resolution.Uri
import resolution.UriResolutionContext

// TODO: Need a better solution for InvokeOptions than having two args parameters (maybe DSL + UBytArray?)
/**
 * Options required for a wrapper invocation.
 *
 * @property uri The Wrapper's URI
 * @property method Method to be executed
 * @property args Arguments for the method, structured as a map, removing the chance of incorrectly ordered arguments
 * @property env Env variables for the wrapper invocation
 * @property resolutionContext A Uri resolution context
 * @property encodeResult If true, the [InvokeResult] will (if successful) contain a Msgpack-encoded byte array.
 */
@OptIn(ExperimentalUnsignedTypes::class)
data class InvokeOptions(
    val uri: Uri,
    val method: String,
    val args: Map<String, Any>? = null,
    val env: Map<String, Any>? = null,
    val resolutionContext: UriResolutionContext? = null,
    val encodeResult: Boolean = false,
    val encodedArgs: UByteArray? = null,
) {
    init {
        if (args != null && encodedArgs != null) {
            throw IllegalArgumentException("Cannot specify both args and encodedArgs")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InvokeOptions

        if (uri != other.uri) return false
        if (method != other.method) return false
        if (args != other.args) return false
        if (env != other.env) return false
        if (resolutionContext != other.resolutionContext) return false
        if (encodeResult != other.encodeResult) return false
        if (encodedArgs != null) {
            if (other.encodedArgs == null) return false
            if (!encodedArgs.contentEquals(other.encodedArgs)) return false
        } else if (other.encodedArgs != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + (args?.hashCode() ?: 0)
        result = 31 * result + (env?.hashCode() ?: 0)
        result = 31 * result + (resolutionContext?.hashCode() ?: 0)
        result = 31 * result + encodeResult.hashCode()
        result = 31 * result + (encodedArgs?.contentHashCode() ?: 0)
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
 *
 * @param TData Type of the invoke result data.
 */
interface Invoker {
    /**
     * Invoke a wrapper using an instance of the wrapper.
     *
     * @param wrapper An instance of a Wrapper to invoke.
     * @param options Invoker options to set and a Wrapper instance to invoke.
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