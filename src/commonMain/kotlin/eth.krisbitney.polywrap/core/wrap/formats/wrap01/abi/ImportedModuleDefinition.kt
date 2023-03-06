package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A data class representing an imported module definition in a Wrap ABI.
 *
 * @property type The type of the imported module definition.
 * @property kind The kind of the imported module definition.
 * @property name The name of the imported module definition.
 * @property required Whether the imported module definition is required.
 * @property uri The URI of the imported module definition.
 * @property namespace The namespace of the imported module definition.
 * @property nativeType The native type of the imported module definition.
 * @property comment The comment of the imported module definition.
 * @property methods The list of methods defined in the imported module definition.
 * @property isInterface Whether the imported module definition is an interface.
 */
@Serializable
data class ImportedModuleDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = null,
    override val uri: String,
    override val namespace: String,
    override val nativeType: String,
    override val comment: String? = null,
    val methods: List<MethodDefinition>? = null,
    val isInterface: Boolean = false
) : IGenericDefinition, ImportedDefinition, WithComment