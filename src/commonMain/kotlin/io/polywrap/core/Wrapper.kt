package io.polywrap.core

import uniffi.polywrap_native.FfiException
import uniffi.polywrap_native.FfiInvoker
import uniffi.polywrap_native.FfiWrapper

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
     * @return A list of MessagePack-encoded bytes representing the invocation result
     * @throws FfiException
     */
    override fun invoke(
        method: String,
        args: List<UByte>?,
        env: List<UByte>?,
        invoker: FfiInvoker
    ): List<UByte>

    /**
     * Invokes a method in the Wrapper with the specified options and invoker.
     *
     * @param method The method to be called on the wrapper.
     * @param args Arguments for the method, encoded in the MessagePack byte format
     * @param env Env variables for the wrapper invocation, encoded in the MessagePack byte format
     * @param invoker The [Invoker] will be used for any sub-invocations that occur.
     * @return A [Result] containing a MsgPack encoded byte array or an error.
     */
    fun invoke(
        method: String,
        args: ByteArray? = null,
        env: ByteArray? = null,
        invoker: Invoker
    ): Result<ByteArray>


    companion object {
        @OptIn(ExperimentalUnsignedTypes::class)
        fun fromFfi(ffiWrapper: FfiWrapper): Wrapper = object : Wrapper {
            override fun invoke(
                method: String,
                args: List<UByte>?,
                env: List<UByte>?,
                invoker: FfiInvoker
            ): List<UByte> = ffiWrapper.invoke(method, args, env, invoker)

            override fun invoke(
                method: String,
                args: ByteArray?,
                env: ByteArray?,
                invoker: Invoker
            ): Result<ByteArray> = runCatching {
                ffiWrapper.invoke(
                    method = method,
                    args = args?.asUByteArray()?.toList(),
                    env = env?.asUByteArray()?.toList(),
                    invoker = invoker.ffiInvoker
                ).toUByteArray().asByteArray()
            }
        }
    }
}