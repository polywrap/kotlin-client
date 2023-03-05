package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * An imported enumeration definition in a Wrap ABI.
 *
 * @property type The type of this definition, which is always "enum".
 * @property kind The kind of this definition.
 * @property name The name of this definition, if it has one.
 * @property required Whether this definition is required.
 * @property comment The comment associated with this definition, if there is one.
 * @property uri The URI where this definition is located.
 * @property namespace The namespace of this definition.
 * @property nativeType The native type of this definition.
 * @property constants The list of constant values defined in this enumeration.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class ImportedEnumDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    @EncodeDefault override val required: Boolean = false,
    override val comment: String? = null,
    override val uri: String,
    override val namespace: String,
    override val nativeType: String,
    val constants: List<String>? = null,
) : IGenericDefinition, WithComment, ImportedDefinition