package eth.krisbitney.polywrap.core.wrap.formats.wrap01

import eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi.Abi

/**
 * A data class representing a Wrap Manifest, which describes a WRAP package.
 * @property version The version of the WRAP standard used in this package.
 * @property type The type of wrapper package.
 * @property name The name of the wrapper package.
 * @property abi The ABI (Application Binary Interface) for this package.
 */
data class WrapManifest(
    val version: String,
    val type: String,
    val name: String,
    val abi: Abi
)