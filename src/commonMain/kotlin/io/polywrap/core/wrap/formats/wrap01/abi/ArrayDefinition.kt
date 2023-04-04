package io.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an array definition in a Wrap ABI.
 *
 * @property type The type of the array definition.
 * @property kind The kind of the array definition.
 * @property name The name of the array definition, if any.
 * @property required A flag indicating if the array definition is required.
 * @property array An optional nested [ArrayDefinition] within the current definition.
 * @property map An optional [MapDefinition] for this definition.
 * @property scalar An optional [ScalarDefinition] for this definition.
 * @property _object An optional object reference for this definition.
 * @property enum An optional enum reference for this definition.
 * @property unresolvedObjectOrEnum An optional unresolved reference for this definition.
 * @property item The type of item stored in the array.
 */
@Serializable
data class ArrayDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = false,
    override val array: ArrayDefinition? = null,
    override val map: MapDefinition? = null,
    override val scalar: ScalarDefinition? = null,
    @SerialName("object")
    override val _object: GenericDefinition? = null,
    override val enum: GenericDefinition? = null,
    override val unresolvedObjectOrEnum: GenericDefinition? = null,
    val item: GenericDefinition? = null
) : AnyDefinition
