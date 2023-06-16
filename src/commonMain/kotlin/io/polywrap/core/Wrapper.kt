package io.polywrap.core

import uniffi.main.FfiAbortHandlerWrapping
import uniffi.main.FfiException
import uniffi.main.FfiInvoker
import uniffi.main.FfiWrapper
import kotlin.jvm.Throws

/**
 * The Wrapper definition, which can be used to invoke this particular Wrapper.
 */
interface Wrapper : FfiWrapper {
    /**
     * Invokes a method in the Wrapper with the specified options and invoker.
     *
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param invoker The invoker will be used for any sub-invocations that occur.
     * @param abortHandler An [AbortHandler] to be called when the invocation is aborted.
     * @return A list of MessagePack-encoded bytes representing the invocation result
     * @throws FfiException
     */
    @Throws(FfiException::class)
    override fun invoke(
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        invoker: FfiInvoker,
        abortHandler: FfiAbortHandlerWrapping?
    ): List<UByte>

    /**
     * Invokes a method in the Wrapper with the specified options and invoker.
     *
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param invoker The [Invoker] will be used for any sub-invocations that occur.
     * @param abortHandler An [AbortHandler] to be called when the invocation is aborted.
     * @return A [Result] containing a MsgPack encoded byte array or an error.
     */
    fun invoke(
        method: String,
        args: ByteArray? = null,
        env: ByteArray? = null,
        invoker: Invoker,
        abortHandler: AbortHandler? = null
    ): Result<ByteArray>


    companion object {
        @OptIn(ExperimentalUnsignedTypes::class)
        fun fromFfi(ffiWrapper: FfiWrapper): Wrapper = object : Wrapper {
            override fun invoke(
                method: String,
                args: List<UByte>?,
                env: List<UByte>?,
                invoker: FfiInvoker,
                abortHandler: FfiAbortHandlerWrapping?
            ): List<UByte> = ffiWrapper.invoke(method, args, env, invoker, abortHandler)

            override fun invoke(
                method: String,
                args: ByteArray?,
                env: ByteArray?,
                invoker: Invoker,
                abortHandler: AbortHandler?
            ): Result<ByteArray> = runCatching {
                ffiWrapper.invoke(
                    method = method,
                    args = args?.asUByteArray()?.toList(),
                    env = env?.asUByteArray()?.toList(),
                    invoker = invoker.ffiInvoker,
                    abortHandler = abortHandler?.let { FfiAbortHandlerWrapping(it) }
                ).toUByteArray().asByteArray()
            }
        }
    }
}