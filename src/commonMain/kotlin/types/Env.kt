package types

import resolution.Uri

/**
 * A map of string-indexed, Msgpack-serializable environmental variables associated with a wrapper.
 * @property uri the URI of the wrapper
 * @property env the environmental variables used by the module
 */
data class Env(
    val uri: Uri,
    val env: Map<String, Any>
)