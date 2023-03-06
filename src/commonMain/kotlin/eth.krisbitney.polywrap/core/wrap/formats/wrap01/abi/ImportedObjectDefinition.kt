package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A data class representing an imported object definition in a Wrap ABI.
 *
 * @property type The type of the imported object definition.
 * @property kind The kind of the imported object definition.
 * @property name The name of the imported object definition.
 * @property required A flag indicating whether this definition is required.
 * @property comment The comment for this definition.
 * @property uri The URI of the imported object.
 * @property namespace The namespace of the imported object.
 * @property nativeType The native type of the imported object.
 * @property properties The list of properties of the imported object.
 * @property interfaces The list of interfaces implemented by the imported object.
 */
@Serializable
data class ImportedObjectDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = null,
    override val comment: String? = null,
    override val uri: String,
    override val namespace: String,
    override val nativeType: String,
    val properties: List<PropertyDefinition>? = null,
    val interfaces: List<GenericDefinition>? = null
) : IGenericDefinition, WithComment, ImportedDefinition