package io.polywrap.core

import uniffi.main.FfiAbortHandlerWrapping
import uniffi.main.FfiException

class DefaultAbortHandler : AbortHandler {
    /**
     * @throws FfiException
     */
    @Throws(FfiException::class)
    override fun abort(msg: String) {
        throw Exception(msg)
    }
}

fun AbortHandler.wrap(): FfiAbortHandlerWrapping = FfiAbortHandlerWrapping(this)
