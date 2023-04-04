@file:JsModule("@polywrap/asyncify-js")
@file:JsNonModule

package io.polywrap.externals

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Json
import kotlin.js.Promise

/**
 * An external class representing an asynchronous WebAssembly instance.
 */
external class AsyncWasmInstance {

    /**
     * Interface for the exports of a WebAssembly module.
     */
    interface Exports {
        /**
         * Executes the '_wrap_invoke' function within the WebAssembly module.
         * @param nameLen The length of the function name.
         * @param argsLen The length of the arguments.
         * @param envLen The length of the environment.
         * @return A [Promise] of type [Int] with the result of the function execution.
         */
        suspend fun _wrap_invoke(nameLen: Int, argsLen: Int, envLen: Int): Promise<Int>
    }

    /** Interface representing the expected structure of Wasm Imports */
    interface Imports : WebAssembly.Imports, Json {
        var wrap: Json
        var env: Env
    }

    /** Interface representing the expected structure of the Env portion of Wasm Imports */
    interface Env : Json {
        var memory: WebAssembly.Memory
    }

    /** Interface representing the arguments for [createMemory] */
    interface createMemoryArgs : Json {
        var module: ByteArray
    }

    /** Interface representing the arguments for [createInstance] */
    interface createInstanceArgs : Json {
        var module: ByteArray
        var imports: Json
        var requiredExports: List<String>?
    }

    /**
     * Companion object containing factory methods for creating WebAssembly instances.
     */
    companion object {
        /**
         * Creates a WebAssembly memory object.
         * @param config The configuration object containing the module as a [ByteArray].
         * @return A [WebAssembly.Memory] object.
         */
        fun createMemory(config: createMemoryArgs): WebAssembly.Memory
        /**
         * Creates an asynchronous WebAssembly instance.
         * @param config The configuration object containing the module as a [ByteArray], imports, and an optional list of required exports.
         * @return A [Promise] containing the created [AsyncWasmInstance].
         */
        fun createInstance(config: createInstanceArgs): Promise<AsyncWasmInstance>
    }

    /**
     * Property holding the exports of the WebAssembly module.
     */
    var exports: Exports
}

/**
 * An external object representing the WebAssembly namespace.
 */
external object WebAssembly {
    /**
     * Interface for the WebAssembly memory object.
     */
    interface Memory {
        /**
         * Property holding the memory buffer of the WebAssembly instance.
         */
        var buffer: ArrayBuffer
    }

    /**
     * Interface for WebAssembly imports.
     */
    interface Imports
}
