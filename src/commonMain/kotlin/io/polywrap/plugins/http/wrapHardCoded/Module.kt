package io.polywrap.plugins.http.wrapHardCoded

import io.polywrap.core.types.Invoker
import io.polywrap.msgpack.msgPackDecode
import io.polywrap.msgpack.msgPackEncode
import io.polywrap.plugin.PluginMethod
import io.polywrap.plugin.PluginModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ArgsGet(
    val url: String,
    val request: HttpRequest? = null
)

@Serializable
data class ArgsPost(
    val url: String,
    val request: HttpRequest? = null
)

abstract class Module<TConfig>(config: TConfig) : PluginModule<TConfig>(config) {

    final override val methods: Map<String, PluginMethod> = mapOf(
        "get" to ::__get,
        "post" to ::__post
    )

    abstract suspend fun get(
        args: ArgsGet,
        invoker: Invoker
    ): HttpResponse?

    abstract suspend fun post(
        args: ArgsPost,
        invoker: Invoker
    ): HttpResponse?

    private suspend fun __get(
        encodedArgs: ByteArray?,
        invoker: Invoker,
        encodedEnv: ByteArray?
    ): ByteArray {
        val args: ArgsGet = encodedArgs?.let { msgPackDecode(ArgsGet.serializer(), it).getOrNull() }
            ?: throw Error("Missing args in invocation to plugin method 'get'")
        val response = get(args, invoker)
        return msgPackEncode(serializer(), response)
    }

    private suspend fun __post(
        encodedArgs: ByteArray?,
        invoker: Invoker,
        encodedEnv: ByteArray?
    ): ByteArray {
        val args: ArgsPost = encodedArgs?.let { msgPackDecode(ArgsPost.serializer(), it).getOrNull() }
            ?: throw Error("Missing args in invocation to plugin method 'post'")
        val response = post(args, invoker)
        return msgPackEncode(serializer(), response)
    }
}
