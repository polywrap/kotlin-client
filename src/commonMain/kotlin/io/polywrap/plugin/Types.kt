package io.polywrap.plugin

import io.polywrap.core.types.Invoker

/**
 * A typealias for a function representing a plugin method.
 * @param args The input arguments as a byte array in MessagePack format.
 * @param invoker The [Invoker] instance used to do the invocation.
 * @param env The [WrapperEnv] as a byte array in MessagePack format.
 * @return The result of the plugin method as a byte array in MessagePack format.
 */
typealias PluginMethod = suspend (args: ByteArray?, invoker: Invoker, env: ByteArray?) -> ByteArray

/**
 * A typealias for a function representing a plugin factory, used to produce plugin instances
 * @param TConfig The type of the configuration object for the plugin module.
 * @param config The configuration object for the plugin module.
 * @return A [PluginPackage] instance with the specified configuration type [TConfig].
 */
typealias PluginFactory<TConfig> = (config: TConfig) -> PluginPackage<TConfig>
