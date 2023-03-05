package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * A serializable class representing a module definition in a Wrap ABI.
 *
 * @property type The type of the module definition.
 * @property kind The kind of the definition.
 * @property name The name of the module definition.
 * @property required A boolean indicating whether the module definition is required or not.
 * @property comment An optional comment for the module definition.
 * @property methods A list of [MethodDefinition] objects representing the methods defined in the module.
 * @property imports A list of [ImportedModuleRef] objects representing imported modules in the module.
 * @property interfaces A list of [GenericDefinition] objects representing the interfaces implemented by the module.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class ModuleDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    @EncodeDefault override val required: Boolean = false,
    override val comment: String? = null,
    val methods: List<MethodDefinition>? = null,
    val imports: List<ImportedModuleRef>? = null,
    val interfaces: List<GenericDefinition>? = null
) : IGenericDefinition, WithComment {

    /**
     * A sealed interface representing an imported module reference in a Wrap ABI.
     *
     * @property type The type of the imported module reference.
     */
    @Serializable
    data class ImportedModuleRef(val type: String? = null)
}