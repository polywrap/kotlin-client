@file:JsModule("@polywrap/asyncify-js")
@file:JsNonModule

package eth.krisbitney.polywrap.externals

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Json
import kotlin.js.Promise

// AsyncWasmInstance declarations
external class AsyncWasmInstance {

    interface Exports {
        suspend fun _wrap_invoke(nameLen: Int, argsLen: Int, envLen: Int): Promise<Int>
    }

    interface Imports : WebAssembly.Imports, Json {
        var wrap: Json
        var env: Env
    }

    interface Env : Json {
        var memory: WebAssembly.Memory
    }

    interface createMemoryArgs {
        var module: ByteArray
    }

    interface createInstanceArgs {
        var module: ByteArray
        var imports: Json
        var requiredExports: List<String>?
    }

    companion object {
        fun createMemory(config: createMemoryArgs): WebAssembly.Memory
        fun createInstance(config: createInstanceArgs): Promise<AsyncWasmInstance>
    }

    var exports: Exports
}

external object WebAssembly {
    interface Memory {
        var buffer: ArrayBuffer
    }

    interface Imports
}