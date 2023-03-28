import eth.krisbitney.polywrap.core.types.Invoker
import eth.krisbitney.polywrap.msgpack.msgPackDecode
import eth.krisbitney.polywrap.msgpack.msgPackEncode
import eth.krisbitney.polywrap.plugin.PluginFactory
import eth.krisbitney.polywrap.plugin.PluginMethod
import eth.krisbitney.polywrap.plugin.PluginModule
import eth.krisbitney.polywrap.plugin.PluginPackage
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

val mockPlugin: PluginFactory<MockPlugin.Config?> = { config: MockPlugin.Config? ->
    PluginPackage(
        pluginModule = MockPlugin(config),
        manifest = mockManifest
    )
}

class MockPlugin(config: Config? = null) : Module<MockPlugin.Config?>(config) {
    class Config()

    override suspend fun add(args: ArgsAdd, invoker: Invoker): Int {
        return args.num + args.ber
    }

    override suspend fun concat(args: ArgsConcat, invoker: Invoker): String {
        return args.str + args.ing
    }
}

@Serializable
data class ArgsAdd(
    val num: Int,
    val ber: Int
)

@Serializable
data class ArgsConcat(
    val str: String,
    val ing: String
)

abstract class Module<TConfig>(config: TConfig) : PluginModule<TConfig>(config) {

    final override val methods: Map<String, PluginMethod> = mapOf(
        "add" to ::__add,
        "concat" to ::__concat
    )

    abstract suspend fun add(
        args: ArgsAdd,
        invoker: Invoker
    ): Int

    abstract suspend fun concat(
        args: ArgsConcat,
        invoker: Invoker
    ): String

    private suspend fun __add(
        encodedArgs: ByteArray?,
        invoker: Invoker,
        encodedEnv: ByteArray?
    ): ByteArray {
        val args: ArgsAdd = encodedArgs?.let { msgPackDecode(serializer<ArgsAdd>(), it).getOrNull() }
            ?: throw Error("Missing args in invocation to plugin method 'get'")
        val response = add(args, invoker)
        return msgPackEncode(serializer(), response)
    }

    private suspend fun __concat(
        encodedArgs: ByteArray?,
        invoker: Invoker,
        encodedEnv: ByteArray?
    ): ByteArray {
        val args: ArgsConcat = encodedArgs?.let { msgPackDecode(serializer<ArgsConcat>(), it).getOrNull() }
            ?: throw Error("Missing args in invocation to plugin method 'post'")
        val response = concat(args, invoker)
        return msgPackEncode(serializer(), response)
    }
}