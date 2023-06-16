package io.polywrap.core

/** A map of string-indexed, Msgpack-serializable environmental variables associated with a wrapper */
typealias WrapperEnv = Map<String, Any>

/**
 * Result of a Wrapper invocation.
 *
 * @param TData Type of the invoke result data.
 */
typealias InvokeResult<TData> = Result<TData>
