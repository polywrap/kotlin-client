package io.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A sealed interface representing a definition with a comment in an ABI.
 *
 * @property comment The comment associated with the definition.
 */
@Serializable
sealed interface WithComment {
    val comment: String?
}
