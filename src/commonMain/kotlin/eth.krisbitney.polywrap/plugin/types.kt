package eth.krisbitney.polywrap.plugin

import eth.krisbitney.polywrap.core.types.Invoker


typealias PluginFactory<TConfig> = (config: TConfig) -> PluginPackage<TConfig>

typealias PluginMethod = suspend (args: ByteArray?, invoker: Invoker, env: ByteArray?) -> ByteArray
