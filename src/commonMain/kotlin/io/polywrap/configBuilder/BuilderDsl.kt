package io.polywrap.configBuilder

import io.polywrap.client.PolywrapClient

fun configBuilder(configBuilder: ConfigBuilder? = null, configure: ConfigBuilder.() -> Unit): ConfigBuilder {
    val builder = configBuilder ?: ConfigBuilder()
    return builder.apply(configure)
}

fun polywrapClient(configBuilder: ConfigBuilder? = null, configure: ConfigBuilder.() -> Unit): PolywrapClient {
    return configBuilder(configBuilder, configure).build()
}
