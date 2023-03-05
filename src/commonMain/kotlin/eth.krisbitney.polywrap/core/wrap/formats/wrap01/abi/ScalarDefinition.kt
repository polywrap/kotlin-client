package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * Definition of a scalar in a Wrap ABI.
 * A scalar is a primitive data type that represents a single value.
 * @property type Type of the scalar. Must be one of "UInt", "UInt8", "UInt16", "UInt32", "Int",
 * "Int8", "Int16", "Int32", "String", "Boolean", "Bytes", "BigInt", "BigNumber", or "JSON".
 * @property kind Kind of the definition.
 * @property name Optional name of the scalar.
 * @property required Boolean indicating whether the scalar is required or not.
 * @throws IllegalArgumentException if the type provided is not valid.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class ScalarDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    @EncodeDefault override val required: Boolean = false,
) : IGenericDefinition {

    /**
     * Validates that the type provided is valid for a scalar definition.
     * @throws IllegalArgumentException if the type provided is not valid.
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
            "String",
            "Boolean",
            "Bytes",
            "BigInt",
            "BigNumber",
            "JSON" -> {}
            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    }
}