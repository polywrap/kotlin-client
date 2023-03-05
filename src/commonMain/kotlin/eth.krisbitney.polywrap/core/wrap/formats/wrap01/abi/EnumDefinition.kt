package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * Represents an enumeration definition in a Wrap ABI.
 *
 * @property type The type of the enumeration definition.
 * @property kind The kind of the enumeration definition.
 * @property name The name of the enumeration definition, if any.
 * @property required A flag indicating if the enumeration definition is required.
 * @property comment An optional comment for the enumeration definition.
 * @property constants The list of constant values defined by the enumeration.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class EnumDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    @EncodeDefault override val required: Boolean = false,
    override val comment: String? = null,
    val constants: List<String>? = null
) : IGenericDefinition, WithComment