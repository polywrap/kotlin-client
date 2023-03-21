package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A class representing an environment definition in a Wrap ABI.
 *
 * @property type The type of the environment definition.
 * @property kind The kind of the environment definition.
 * @property name The name of the environment definition, if any.
 * @property required A flag indicating if the environment definition is required.
 * @property comment An optional comment about the environment definition.
 * @property properties The list of property definitions for the environment.
 * @property interfaces The list of generic interface definitions for the environment.
 */
@Serializable
data class EnvDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = false,
    override val comment: String? = null,
    val properties: List<PropertyDefinition>? = null,
    val interfaces: List<GenericDefinition>? = null
) : IGenericDefinition, WithComment