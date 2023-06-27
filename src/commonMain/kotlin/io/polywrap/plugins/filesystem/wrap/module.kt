/// NOTE: This is an auto-generated file.
///       All modifications will be overwritten.

package io.polywrap.plugins.filesystem.wrap

import io.polywrap.core.Invoker
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.plugin.PluginMethod
import io.polywrap.plugin.PluginModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ArgsReadFile(
    val path: String,
)

@Serializable
data class ArgsReadFileAsString(
    val path: String,
    val encoding: Encoding? = null,
)

@Serializable
data class ArgsExists(
    val path: String,
)

@Serializable
data class ArgsWriteFile(
    val path: String,
    val data: ByteArray,
)

@Serializable
data class ArgsMkdir(
    val path: String,
    val recursive: Boolean? = null,
)

@Serializable
data class ArgsRm(
    val path: String,
    val recursive: Boolean? = null,
    val force: Boolean? = null,
)

@Serializable
data class ArgsRmdir(
    val path: String,
)

@Suppress("UNUSED_PARAMETER", "FunctionName")
abstract class Module<TConfig>(config: TConfig) : PluginModule<TConfig>(config) {

  final override val methods: Map<String, PluginMethod> = mapOf(
      "readFile" to ::__readFile,
      "readFileAsString" to ::__readFileAsString,
      "exists" to ::__exists,
      "writeFile" to ::__writeFile,
      "mkdir" to ::__mkdir,
      "rm" to ::__rm,
      "rmdir" to ::__rmdir,
  )

  abstract suspend fun readFile(
      args: ArgsReadFile,
      invoker: Invoker
  ): ByteArray

  abstract suspend fun readFileAsString(
      args: ArgsReadFileAsString,
      invoker: Invoker
  ): String

  abstract suspend fun exists(
      args: ArgsExists,
      invoker: Invoker
  ): Boolean

  abstract suspend fun writeFile(
      args: ArgsWriteFile,
      invoker: Invoker
  ): Boolean?

  abstract suspend fun mkdir(
      args: ArgsMkdir,
      invoker: Invoker
  ): Boolean?

  abstract suspend fun rm(
      args: ArgsRm,
      invoker: Invoker
  ): Boolean?

  abstract suspend fun rmdir(
      args: ArgsRmdir,
      invoker: Invoker
  ): Boolean?

  private suspend fun __readFile(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsReadFile = encodedArgs?.let {
            msgPackDecode(ArgsReadFile.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'readFile'")
        } ?: throw Exception("Missing args in invocation to plugin method 'readFile'")
        val response = readFile(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __readFileAsString(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsReadFileAsString = encodedArgs?.let {
            msgPackDecode(ArgsReadFileAsString.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'readFileAsString'")
        } ?: throw Exception("Missing args in invocation to plugin method 'readFileAsString'")
        val response = readFileAsString(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __exists(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsExists = encodedArgs?.let {
            msgPackDecode(ArgsExists.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'exists'")
        } ?: throw Exception("Missing args in invocation to plugin method 'exists'")
        val response = exists(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __writeFile(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsWriteFile = encodedArgs?.let {
            msgPackDecode(ArgsWriteFile.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'writeFile'")
        } ?: throw Exception("Missing args in invocation to plugin method 'writeFile'")
        val response = writeFile(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __mkdir(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsMkdir = encodedArgs?.let {
            msgPackDecode(ArgsMkdir.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'mkdir'")
        } ?: throw Exception("Missing args in invocation to plugin method 'mkdir'")
        val response = mkdir(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __rm(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsRm = encodedArgs?.let {
            msgPackDecode(ArgsRm.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'rm'")
        } ?: throw Exception("Missing args in invocation to plugin method 'rm'")
        val response = rm(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __rmdir(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsRmdir = encodedArgs?.let {
            msgPackDecode(ArgsRmdir.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'rmdir'")
        } ?: throw Exception("Missing args in invocation to plugin method 'rmdir'")
        val response = rmdir(args, invoker)
        return msgPackEncode(serializer(), response)
  }
}
