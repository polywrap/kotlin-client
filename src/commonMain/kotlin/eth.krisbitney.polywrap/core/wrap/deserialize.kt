package eth.krisbitney.polywrap.core.wrap

import eth.krisbitney.polywrap.core.wrap.formats.wrap01.WrapManifest01
import kotlinx.serialization.decodeFromByteArray

/**
 * Deserializes a given [manifest] represented as a [ByteArray] to a [WrapManifest] object.
 * @param manifest the serialized manifest to deserialize.
 * @return the deserialized [WrapManifest] object.
 * @throws Error if the given manifest is not a valid WrapManifest or if it's unable to be parsed.
 */
fun deserializeWrapManifest(manifest: ByteArray): Result<WrapManifest> {
    val wrapManifest: WrapManifest01
    try {
        wrapManifest = manifestMsgPack.decodeFromByteArray(manifest)
    } catch (e: Throwable) {
        val message = "Unable to parse WrapManifest: ${e.message}"
        return Result.failure(IllegalArgumentException(message))
    }

    val supportedManifestVersions: List<String> = WrapManifestVersions.values().map { it.value }
    if (!supportedManifestVersions.contains(wrapManifest.version)) {
        val message = "Unsupported WrapManifest version: ${wrapManifest.version}"
        return Result.failure(IllegalArgumentException(message))
    }

    // todo: check with semver and migrate

    return Result.success(wrapManifest)
}
