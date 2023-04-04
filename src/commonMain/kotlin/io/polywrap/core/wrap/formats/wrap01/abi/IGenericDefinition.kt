package io.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A sealed interface representing a generic definition in a Wrap ABI.
 *
 * @property type The type of the generic definition.
 * @property kind The kind of the generic definition.
 * @property name The name of the generic definition, if any.
 * @property required A flag indicating if the generic definition is required.
 */
@Serializable
sealed interface IGenericDefinition {
    val type: String
    val kind: Int
    val name: String?
    val required: Boolean?
}
