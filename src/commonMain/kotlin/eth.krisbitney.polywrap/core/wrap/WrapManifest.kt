package eth.krisbitney.polywrap.core.wrap

import eth.krisbitney.polywrap.core.wrap.formats.wrap01.WrapManifest01


typealias WrapManifest = WrapManifest01

/**
 * Enum representing the versions of WrapManifest.
 * @property value The value of the version as a string.
 */
enum class WrapManifestVersions(val value: String) {
    // NOTE: Patch fix for backwards compatibility.
    V0_1_0("0.1.0"),
    V0_1("0.1")
}

val latestWrapManifestVersion: String = WrapManifestVersions.V0_1.value
