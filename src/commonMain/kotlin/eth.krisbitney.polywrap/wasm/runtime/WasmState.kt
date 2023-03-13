package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.core.types.ErrorSource
import eth.krisbitney.polywrap.core.types.Invoker

/**
 * Represents the state of a WebAssembly module, including method name, arguments, environment, invoke state, subinvoke state,
 * implementation result, abort functions, and invoker.
 *
 * @param method The name of the method.
 * @param args An array of bytes representing the arguments to the method.
 * @param env An array of bytes representing the environment for the method.
 * @param invoke An instance of [InvokeState] representing the invoke state of the module.
 * @param subinvoke An instance of [InvokeState] representing the subinvoke state of the module.
 * @param getImplementationsResult A nullable array of bytes representing the implementation result of the module.
 * @param abortWithInvokeAborted A function that throws an exception with an optional message and error source if the module's invocation is aborted.
 * @param abortWithInternalError A function that throws an exception with an error message if the module encounters an internal error.
 * @param invoker An instance of [Invoker] used to invoke the module's methods.
 */
class WasmModuleState(
    val method: String,
    val args: ByteArray,
    val env: ByteArray,
    val invoke: InvokeState = InvokeState(),
    val subinvoke: InvokeState = InvokeState(),
    var getImplementationsResult: ByteArray? = null,
    val abortWithInvokeAborted: (message: String, source: ErrorSource?) -> Nothing,
    val abortWithInternalError: (message: String) -> Nothing,
    val invoker: Invoker,
) {

    /**
     * Represents the invoke state of a WebAssembly module, including the result and error message.
     *
     * @property result A nullable array of bytes representing the result of the invoke operation.
     * @property error A nullable string representing any error message associated with the invoke operation.
     */
    class InvokeState(
        var result: ByteArray? = null,
        var error: String? = null
    )
}