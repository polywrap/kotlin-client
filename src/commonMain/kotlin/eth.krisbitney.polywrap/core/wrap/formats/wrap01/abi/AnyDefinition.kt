package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Interface representing any definition in a Wrap ABI.
 * @property array An optional [ArrayDefinition] for this definition.
 * @property scalar An optional [ScalarDefinition] for this definition.
 * @property map An optional [MapDefinition] for this definition.
 * @property obj An optional object reference for this definition.
 * @property enum An optional enum reference for this definition.
 * @property unresolvedObjectOrEnum An optional unresolved reference for this definition.
 */
@Serializable
sealed interface AnyDefinition : IGenericDefinition {
    val array: ArrayDefinition?
    val scalar: ScalarDefinition?
    val map: MapDefinition?
    @SerialName("object")
    val _object: GenericDefinition?
    val enum: GenericDefinition?
    val unresolvedObjectOrEnum: IGenericDefinition?
}