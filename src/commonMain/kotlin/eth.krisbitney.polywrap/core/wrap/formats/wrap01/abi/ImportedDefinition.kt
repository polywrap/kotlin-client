package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A sealed interface representing an imported definition in a Wrap ABI.
 *
 * @property uri The URI of the imported definition.
 * @property namespace The namespace of the imported definition.
 * @property nativeType The native type of the imported definition.
 */
@Serializable
sealed interface ImportedDefinition {
    val uri: String
    val namespace: String
    val nativeType: String
}
