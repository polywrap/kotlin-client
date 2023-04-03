package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A data class representing a method definition in a Wrap ABI.
 * @property type The type of the method.
 * @property kind The kind of the method definition.
 * @property name The name of the method.
 * @property required A boolean indicating whether the method is required or not.
 * @property comment An optional comment associated with the method.
 * @property arguments A list of [PropertyDefinition] objects representing the arguments of the method.
 * @property env An optional [EnvRequired] object representing the environment required for the method.
 * @property _return An optional [PropertyDefinition] object representing the return value of the method.
 */
@Serializable
data class MethodDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = false,
    override val comment: String? = null,
    val arguments: List<PropertyDefinition>? = null,
    val env: EnvRequired? = null,
    @SerialName("return")
    val _return: PropertyDefinition? = null
) : IGenericDefinition, WithComment {

    /**
     * A data class representing whether an environment is required for a method in a Wrap ABI.
     *
     * @property required A boolean indicating whether the environment is required.
     */
    @Serializable
    data class EnvRequired(val required: Boolean? = null)
}
