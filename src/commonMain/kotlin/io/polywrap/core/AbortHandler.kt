package io.polywrap.core

import uniffi.main.FfiAbortHandler

typealias AbortHandler = FfiAbortHandler

class DefaultAbortHandler : AbortHandler {
    /**
     * @throws Exception
     */
    override fun abort(msg: String) {
        throw Exception(msg)
    }

}