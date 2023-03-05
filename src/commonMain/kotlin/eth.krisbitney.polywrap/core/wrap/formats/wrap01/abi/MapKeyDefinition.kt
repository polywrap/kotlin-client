package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * Definition of a Map key in a Wrap ABI.
 * @property type Type of the key.
 * @property kind Kind of the definition.
 * @property name Optional name of the key.
 * @property required Boolean indicating whether the key is required or not.
 * @property array Definition of the inner array if the key is of array type.
 * @property scalar Definition of the inner scalar if the key is of scalar type.
 * @property map Definition of the inner map if the key is of map type.
 * @property obj Reference to the object if the key is of object type.
 * @property enum Reference to the enum if the key is of enum type.
 * @property unresolvedObjectOrEnum Reference to the unresolved object or enum if the key is of an unresolved type.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class MapKeyDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    @EncodeDefault override val required: Boolean = false,
    override val array: ArrayDefinition? = null,
    override val scalar: ScalarDefinition? = null,
    override val map: MapDefinition? = null,
    override val obj: GenericDefinition? = null,
    override val enum: GenericDefinition? = null,
    override val unresolvedObjectOrEnum: GenericDefinition? = null,
) : AnyDefinition {

    /**
     * Verifies if the MapKeyDefinition type is valid.
     * @throws IllegalArgumentException if the type is invalid.
     */
    init {
        when (type) {
            "UInt",
            "UInt8",
            "UInt16",
            "UInt32",
            "Int",
            "Int8",
            "Int16",
            "Int32",
            "String" -> {}
            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    }
}