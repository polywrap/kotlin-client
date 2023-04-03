package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A data class implementing [IGenericDefinition].
 *
 * @property type The type of the generic definition.
 * @property kind The kind of the generic definition.
 * @property name The name of the generic definition, if any.
 * @property required A flag indicating if the generic definition is required.
 */
@Serializable
data class GenericDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = false
) : IGenericDefinition
