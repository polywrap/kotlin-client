package io.polywrap.configBuilder

import io.polywrap.core.resolution.Uri
import io.polywrap.wasm.WasmPackage

fun validateUri(uri: String): String = Uri(uri).uri

class ResourceReader {
    companion object {
        fun readResource(path: String): Result<ByteArray> = runCatching {
            ClassLoader.getSystemResourceAsStream(path)?.use {
                it.readBytes()
            } ?: throw Exception("Resource not found: $path")
        }

        fun readWasmPackage(name: String): WasmPackage {
            val module: ByteArray = readResource("embeds/$name/wrap.wasm").getOrThrow()
            val manifest: ByteArray = readResource("embeds/$name/wrap.info").getOrThrow()
            return WasmPackage(manifest, module)
        }
    }
}
