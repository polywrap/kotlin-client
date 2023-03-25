package eth.krisbitney.polywrap.wasm.runtime

interface WrapExports {
    fun _wrap_invoke(nameLen: Int, argsLen: Int, envLen: Int): Int
}