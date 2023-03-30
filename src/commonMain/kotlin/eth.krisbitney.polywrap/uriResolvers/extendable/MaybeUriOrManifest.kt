package eth.krisbitney.polywrap.uriResolvers.extendable

import kotlinx.serialization.Serializable

/**
 * Data class representing a Uri, a manifest, or neither.
 * @property uri the Wrap Uri associated with the resource, or null if the resource is not a Uri.
 * @property manifest the serialized Wrap manifest associated with the resource, or null if there is no manifest.
 */
@Serializable
data class MaybeUriOrManifest(
    val uri: String?,
    val manifest: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MaybeUriOrManifest

        if (uri != other.uri) return false
        if (manifest != null) {
            if (other.manifest == null) return false
            if (!manifest.contentEquals(other.manifest)) return false
        } else if (other.manifest != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri?.hashCode() ?: 0
        result = 31 * result + (manifest?.contentHashCode() ?: 0)
        return result
    }
}