package eth.krisbitney.polywrap.core.wrap.formats.wrap01

import eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi.Abi01
import kotlinx.serialization.Serializable

/**
 * A data class representing a Wrap Manifest, which describes a WRAP package.
 * @property version The version of the WRAP standard used in this package.
 * @property type The type of wrapper package.
 * @property name The name of the wrapper package.
 * @property abi The ABI (Application Binary Interface) for this package.
 */
@Serializable
data class WrapManifest01(
    val version: String,
    val type: String,
    val name: String,
    val abi: Abi01
) {

    init {
        require(version == "0.1.0" || version == "0.1") { "Unsupported WrapManifest version: $version. Expected version: 0.1.0" }
        require(type == "wasm" || type == "interface" || type == "plugin") { "Unsupported WrapManifest type: $type. Supported types: wasm, plugin, interface" }
        require(Regex("""^[a-zA-Z0-9\-_]+$""").matches(name)) { "WrapManifest name contains invalid characters: $name" }
    }
}