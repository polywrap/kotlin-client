package io.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * Data class representing an imported environment definition in a Wrap ABI.
 *
 * @property type The type of the imported environment definition.
 * @property kind An integer representing the kind of the imported environment definition.
 * @property name The name of the imported environment definition.
 * @property required A boolean value indicating whether the imported environment definition is required.
 * @property comment The comment associated with the imported environment definition.
 * @property uri The URI of the imported environment definition.
 * @property namespace The namespace of the imported environment definition.
 * @property nativeType The native type of the imported environment definition.
 * @property properties A list of [PropertyDefinition] objects representing the properties of the imported environment definition.
 * @property interfaces A list of [GenericDefinition] objects representing the interfaces implemented by the imported environment definition.
 */
@Serializable
data class ImportedEnvDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = false,
    override val comment: String? = null,
    override val uri: String,
    override val namespace: String,
    override val nativeType: String,
    val properties: List<PropertyDefinition>? = null,
    val interfaces: List<GenericDefinition>? = null
) : IGenericDefinition, WithComment, ImportedDefinition
