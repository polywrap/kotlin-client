package io.polywrap.configBuilder

/**
 * Default configuration bundles are defined in the FFI native code.
 * They implement the [Bundle] interface and can be added to a [ConfigBuilder].
 * However, they cannot be used to override other configuration values.
 */
enum class DefaultBundle {
    /**
     * The System bundle contains packages that interface with the host system, including:
     * - filesystem
     * - HTTP
     */
    System,

    /**
     * The Web3 bundle contains packages that interface with decentralized networks like Ethereum and IPFS.
     */
    Web3
}
