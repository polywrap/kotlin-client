package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * Represents an object definition in a Wrap ABI.
 * @property type Type of the object definition.
 * @property name Optional name of the object definition.
 * @property required Boolean indicating whether the object definition is required or not.
 * @property kind Kind of the definition.
 * @property comment Optional comment about the object definition.
 * @property properties List of [PropertyDefinition]s that define the properties of the object.
 * @property interfaces List of interfaces that define the interfaces implemented by the object.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class ObjectDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    @EncodeDefault override val required: Boolean = false,
    override val comment: String? = null,
    val properties: List<PropertyDefinition>? = null,
    val interfaces: List<GenericDefinition>? = null
) : IGenericDefinition, WithComment