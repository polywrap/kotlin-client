package eth.krisbitney.polywrap.wasm.runtime

interface IWrapExports {
    fun _wrap_invoke(nameLen: Int, argsLen: Int, envLen: Int): Int
}

/**
 * Represents the WebAssembly import functions that wrap the underlying platform-specific functions.
 */
interface IWrapImports {
    fun __wrap_subinvoke(): (
        uriPtr: Int,
        uriLen: Int,
        methodPtr: Int,
        methodLen: Int,
        argsPtr: Int,
        argsLen: Int
    ) -> Int

    fun __wrap_subinvoke_result_len(): () -> Int
    fun __wrap_subinvoke_result(): (ptr: Int) -> Unit
    fun __wrap_subinvoke_error_len(): () -> Int
    fun __wrap_subinvoke_error(): (ptr: Int) -> Unit
    fun __wrap_invoke_args(): (methodPtr: Int, argsPtr: Int) -> Unit
    fun __wrap_invoke_result(): (ptr: Int, len: Int) -> Unit
    fun __wrap_invoke_error(): (ptr: Int, len: Int) -> Unit
    fun __wrap_getImplementations(): (uriPtr: Int, uriLen: Int) -> Int
    fun __wrap_getImplementations_result_len(): () -> Int
    fun __wrap_getImplementations_result(): (ptr: Int) -> Unit
    fun __wrap_abort(): (msgPtr: Int, msgLen: Int, filePtr: Int, fileLen: Int, line: Int, column: Int) -> Unit
    fun __wrap_debug_log(): (ptr: Int, len: Int) -> Unit
    fun __wrap_load_env(): (ptr: Int) -> Unit
}
