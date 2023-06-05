/// NOTE: This is an auto-generated file.
///       All modifications will be overwritten.

package io.polywrap.plugins.http.wrap

import io.polywrap.core.Invoker
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.core.msgpack.MsgPackMap
import io.polywrap.plugin.PluginMethod
import io.polywrap.plugin.PluginModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ArgsGet(
    val url: String,
    val request: Request? = null,
)

@Serializable
data class ArgsPost(
    val url: String,
    val request: Request? = null,
)

@Suppress("UNUSED_PARAMETER", "FunctionName")
abstract class Module<TConfig>(config: TConfig) : PluginModule<TConfig>(config) {

  final override val methods: Map<String, PluginMethod> = mapOf(
      "get" to ::__get,
      "post" to ::__post,
  )

  abstract suspend fun get(
      args: ArgsGet,
      invoker: Invoker
  ): Response?

  abstract suspend fun post(
      args: ArgsPost,
      invoker: Invoker
  ): Response?

  private suspend fun __get(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsGet = encodedArgs?.let {
            msgPackDecode(ArgsGet.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'get'")
        } ?: throw Exception("Missing args in invocation to plugin method 'get'")
        val response = get(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __post(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsPost = encodedArgs?.let {
            msgPackDecode(ArgsPost.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'post'")
        } ?: throw Exception("Missing args in invocation to plugin method 'post'")
        val response = post(args, invoker)
        return msgPackEncode(serializer(), response)
  }
}
