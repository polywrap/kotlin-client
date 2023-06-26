package io.polywrap.core

import kotlinx.serialization.Contextual

/** A map of string-indexed, Msgpack-serializable environmental variables associated with a wrapper */
typealias WrapEnv = Map<String, @Contextual Any>

/**
 * Result of a Wrapper invocation.
 *
 * @param TData Type of the invoke result data.
 */
typealias InvokeResult<TData> = Result<TData>
