package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Definition of a property in a Wrap ABI.
 * @property comment Optional comment for the property.
 * @property type Type of the property.
 * @property name Optional name of the property.
 * @property required Boolean indicating whether the property is required or not.
 * @property kind Kind of the property definition.
 * @property array Definition of an array property, if it is an array type.
 * @property scalar Definition of a scalar property, if it is a scalar type.
 * @property map Definition of a map property, if it is a map type.
 * @property _object Reference to an object property, if it is an object type.
 * @property enum Reference to an enum property, if it is an enum type.
 * @property unresolvedObjectOrEnum Reference to an unresolved object or enum property.
 */
@Serializable
data class PropertyDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = false,
    override val comment: String? = null,
    override val array: ArrayDefinition? = null,
    override val scalar: ScalarDefinition? = null,
    override val map: MapDefinition? = null,
    @SerialName("object")
    override val _object: GenericDefinition? = null,
    override val enum: GenericDefinition? = null,
    override val unresolvedObjectOrEnum: GenericDefinition? = null
) : WithComment, AnyDefinition