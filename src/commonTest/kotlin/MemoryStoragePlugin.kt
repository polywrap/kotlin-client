import io.polywrap.core.types.Invoker
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.plugin.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

val memoryStoragePlugin: PluginFactory<MemoryStoragePlugin.Config?> = { config: MemoryStoragePlugin.Config? ->
    PluginPackage(
        pluginModule = MemoryStoragePlugin(config),
        manifest = mockManifest
    )
}

class MemoryStoragePlugin(config: Config? = null) : MemoryStoragePluginModule<MemoryStoragePlugin.Config?>(config) {

    private var value: Int = 0

    class Config()

    override suspend fun getData(invoker: Invoker): Int {
        delay(50)
        return this.value
    }

    override suspend fun setData(args: ArgsSetData, invoker: Invoker): Boolean {
        delay(50)
        this.value = args.value
        return true
    }
}

@Serializable
data class ArgsSetData(val value: Int)

@Suppress("UNUSED_PARAMETER")
abstract class MemoryStoragePluginModule<TConfig>(config: TConfig) : PluginModule<TConfig>(config) {

    final override val methods: Map<String, PluginMethod> = mapOf(
        "getData" to ::__getData,
        "setData" to ::__setData
    )

    abstract suspend fun getData(
        invoker: Invoker
    ): Int

    abstract suspend fun setData(
        args: ArgsSetData,
        invoker: Invoker
    ): Boolean

    private suspend fun __getData(
        encodedArgs: ByteArray?,
        invoker: Invoker,
        encodedEnv: ByteArray?
    ): ByteArray {
        val response = getData(invoker)
        return msgPackEncode(serializer(), response)
    }

    private suspend fun __setData(
        encodedArgs: ByteArray?,
        invoker: Invoker,
        encodedEnv: ByteArray?
    ): ByteArray {
        val args: ArgsSetData = encodedArgs?.let { msgPackDecode(serializer<ArgsSetData>(), it).getOrNull() }
            ?: throw Error("Missing args in invocation to plugin method 'setData'")
        val response = setData(args, invoker)
        return msgPackEncode(serializer(), response)
    }
}
