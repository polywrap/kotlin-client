package eth.krisbitney.polywrap.wasm.runtime

import eth.krisbitney.polywrap.core.types.ErrorSource
import eth.krisbitney.polywrap.core.types.Invoker

class WasmModuleState(
    val method: String,
    val args: ByteArray,
    val env: ByteArray,
    val invoke: InvokeState = InvokeState(),
    val subinvoke: InvokeState = InvokeState(),
    val abortWithInvokeAborted: (message: String, source: ErrorSource?) -> Nothing,
    val abortWithInternalError: (message: String) -> Nothing,
    val invoker: Invoker,
    val getImplementationsResult: ByteArray? = null,
    val subinvokeImplementation: SubInvokeInterfaceImplementationState? = null,
) {

    /**
     * Represents the result and error information for the top-level invoke.
     * @property result The result data of the invocation.
     * @property error The error message, if any, that occurred during the invocation.
     */
    class InvokeState(
        var result: ByteArray? = null,
        var error: String? = null
    )

    class SubInvokeInterfaceImplementationState(
        var result: ByteArray? = null,
        var error: String? = null,
        var args: ByteArray
    )
}