package io.polywrap.plugin

import io.polywrap.core.types.Invoker

typealias PluginMethod = suspend (args: ByteArray?, invoker: Invoker, env: ByteArray?) -> ByteArray

typealias PluginFactory<TConfig> = (config: TConfig) -> PluginPackage<TConfig>
