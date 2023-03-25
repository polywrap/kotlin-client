@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
@file:JsModule("@polywrap/asyncify-js")
@file:JsNonModule

package eth.krisbitney.polywrap.externals

import kotlin.js.Promise

// AsyncWasmInstance declarations
external class AsyncWasmInstance {
    companion object {
        fun createMemory(config: dynamic): dynamic // returns WasmMemory
        fun createInstance(config: dynamic): Promise<AsyncWasmInstance>
    }

    val exports: dynamic // returns WasmExports
}