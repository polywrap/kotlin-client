package io.polywrap.core

import uniffi.main.FfiAbortHandler
import uniffi.main.FfiAbortHandlerWrapping
import uniffi.main.FfiException

typealias AbortHandler = FfiAbortHandler

/**
 * A basic abort handler that throws an exception when invoked.
 */
class DefaultAbortHandler : AbortHandler {
    /**
     * @throws FfiException
     */
    @Throws(FfiException::class)
    override fun abort(msg: String) = throw Exception(msg)
}

internal class WrappedAbortHandler(
    private val ffiAbortHandlerWrapping: FfiAbortHandlerWrapping
) : AbortHandler, AutoCloseable {
    override fun abort(msg: String) = ffiAbortHandlerWrapping.abort(msg)
    override fun close() = ffiAbortHandlerWrapping.close()
}

internal fun AbortHandler.wrap(): FfiAbortHandlerWrapping = FfiAbortHandlerWrapping(this)

internal fun FfiAbortHandlerWrapping.wrap(): AbortHandler = WrappedAbortHandler(this)

