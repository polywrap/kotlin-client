package eth.krisbitney.polywrap.core.types

class ErrorSource(
    val file: String?,
    val row: Int?,
    val col: Int?
)

/**
Wrap error codes provide additional context to WrapErrors.

Error code naming convention (approximate):
type of handler
type of functionality
piece of functionality
==> handler_typeFn_pieceFn

Error code map:
0 -> Invalid
1-25 -> Client
26-50 -> URI resolution
51-75 -> Wrapper invocation & sub-invocation
76-255 -> Unallocated
 */
enum class WrapErrorCode(val value: Int) {
    CLIENT_LOAD_WRAPPER_ERROR(1),
    CLIENT_GET_FILE_ERROR(2),
    CLIENT_GET_IMPLEMENTATIONS_ERROR(3),
    CLIENT_VALIDATE_RESOLUTION_FAIL(4),
    CLIENT_VALIDATE_ABI_FAIL(5),
    CLIENT_VALIDATE_RECURSIVE_FAIL(6),
    URI_RESOLUTION_ERROR(26),
    URI_RESOLVER_ERROR(27),
    URI_NOT_FOUND(28),
    WRAPPER_INVOKE_ABORTED(51),
    WRAPPER_SUBINVOKE_ABORTED(52),
    WRAPPER_INVOKE_FAIL(53),
    WRAPPER_READ_FAIL(54),
    WRAPPER_INTERNAL_ERROR(55),
    WRAPPER_METHOD_NOT_FOUND(56),
    WRAPPER_ARGS_MALFORMED(57);

    companion object {
        private val VALUES = WrapErrorCode.values()
        fun from(value: Int): WrapErrorCode = VALUES.firstOrNull { it.value == value } ?: throw Error("Invalid WrapErrorCode value: $value")
        fun from(value: String): WrapErrorCode {
            val intVal = value.toInt()
            return VALUES.firstOrNull { it.value == intVal } ?: throw Error("Invalid WrapErrorCode value: $value")
        }
    }

    override fun toString(): String = "$value ${this.name.replace("_", " ")}"
}

/**
 * WrapError is a custom error type that provides additional context to errors.
 *
 * @property reason The reason or context for the error.
 * @param cause The exception that caused this exception.
 * @property code The error code.
 * @property uri The URI of the wrapper.
 * @property method The method that caused the error.
 * @property args The arguments passed to the method.
 * @property source The error source (file, row, col).
 * @property resolutionStack The clean resolution step stack.
 * @property innerError A nested WrapError.
 */
class WrapError(
    val reason: String = "An error occurred.",
    cause: Throwable? = null,
    val code: WrapErrorCode,
    val uri: String,
    val method: String? = null,
    val args: String? = null,
    val source: ErrorSource? = null,
    val resolutionStack: String? = null,
    val innerError: WrapError? = null
) : Exception(stringify(reason, cause, code, uri, method, args, source, resolutionStack, innerError), cause) {

    override fun toString(): String {
        return "$name: $message"
    }

    companion object {
        const val name: String = "WrapError"
        private const val delim = "\n\nAnother exception was encountered during execution:\n"

        /**
         * Matches a WrapError in a string and returns its parsed arguments.
         */
        private val re = Regex(
            listOf(
                Regex("^(?:[A-Za-z_:()` ]*;? \"?)?WrapError: (?<reason>(?:.|\r|\n)*)").pattern,
                // there is some padding added to the number of words expected in an error code
                Regex("(?:\r\n|\r|\n)code: (?<code>1?[0-9]{1,2}|2[0-4][0-9]|25[0-5]) (?:[A-Z]+ ?){1,5}").pattern,
                Regex("(?:\r\n|\r|\n)uri: (?<uri>wrap://[A-Za-z0-9_-]+/.+)").pattern,
                Regex("(?:(?:\r\n|\r|\n)method: (?<method>([A-Za-z_]{1}[A-Za-z0-9_]*)))?").pattern,
                Regex("(?:(?:\r\n|\r|\n)args: (?<args>\\{(?:.|\r|\n)+} ))?").pattern,
                Regex("(?:(?:\r\n|\r|\n)source: \\{ file: \"(?<file>.+)\", row: (?<row>[0-9]+), col: (?<col>[0-9]+) })?").pattern,
                Regex("(?:(?:\r\n|\r|\n)uriResolutionStack: (?<resolutionStack>\\[(?:.|\r|\n)+]))?").pattern,
                Regex("(?:(?:\r\n|\r|\n){2}This exception was caused by the following exception:(?:\r\n|\r|\n)(?<cause>(?:.|\r|\n)+))?").pattern,
                Regex("\"?$").pattern
            ).joinToString("")
        )

        private class ParsedWrapError(
            val reason: String,
            val cause: Throwable? = null,
            val code: WrapErrorCode,
            val uri: String,
            val method: String? = null,
            val args: String? = null,
            val source: ErrorSource? = null,
            val resolutionStack: String? = null,
            val innerError: WrapError? = null
        )

        fun parse(error: String): WrapError? {
            val sanitizedError = sanitizeErrorString(error)
            val errorStrings = sanitizedError.split(delim)

            // case: single WrapError or not a WrapError
            if (errorStrings.size == 1) {
                val args = _parse(sanitizedError)
                return args?.let {
                    WrapError(
                        it.reason,
                        it.cause,
                        it.code,
                        it.uri,
                        it.method,
                        it.args,
                        it.source,
                        it.resolutionStack,
                        it.innerError
                    )
                }
            }

            // case: stack of WrapErrors stringified
            val errArgs = errorStrings.map { _parse(it) }

            // iterate through args to assign `cause` and `prev`
            var curr: WrapError? = null
            for (i in errArgs.size - 1 downTo 0) {
                val currArgs = errArgs.getOrNull(i) ?: throw Error("Failed to parse WrapError")
                curr = WrapError(
                    currArgs.reason,
                    currArgs.cause,
                    currArgs.code,
                    currArgs.uri,
                    currArgs.method,
                    currArgs.args,
                    currArgs.source,
                    currArgs.resolutionStack,
                    curr
                )
            }
            return curr
        }

        private fun sanitizeErrorString(error: String): String {
            var sanitizedError = error

            if (sanitizedError.startsWith("__wrap_abort: called `Result::unwrap()` on an `Err` value: \"")) {
                sanitizedError = sanitizedError.replace("\\\"", "\"")
                sanitizedError = sanitizedError.replace("\\n", "\n")
            }

            return sanitizedError
        }

        // parse a single WrapError, where the 'prev' property is undefined
        private fun _parse(error: String): ParsedWrapError? {
            val groups = re.matchEntire(error)?.groups as? MatchNamedGroupCollection ?: return null
            val code = groups["code"]?.value ?: return null
            val reason = groups["reason"]?.value ?: return null
            val uri = groups["uri"]?.value ?: return null
            val method = groups["method"]?.value
            val args = groups["args"]?.value
            val file = groups["file"]?.value
            val row = groups["row"]?.value
            val col = groups["col"]?.value
            val resolutionStack = groups["resolutionStack"]?.value
            val cause = groups["cause"]?.value

            val source = file?.let {
                ErrorSource(
                    file = it,
                    row = row?.toIntOrNull(),
                    col = col?.toIntOrNull()
                )
            }

            return ParsedWrapError(
                reason = reason,
                cause = Error(cause),
                code = WrapErrorCode.from(code),
                uri = uri,
                method = method,
                args = args?.trim(),
                source = source,
                resolutionStack = resolutionStack
            )
        }

        private fun stringify(
            reason: String,
            cause: Throwable?,
            code: WrapErrorCode,
            uri: String,
            method: String? = null,
            args: String? = null,
            source: ErrorSource? = null,
            resolutionStack: String? = null,
            innerError: WrapError? = null
        ): String {
            val maybeMethod = method?.let { "method: $it" } ?: ""
            val maybeArgs = args?.let { "args: $it " } ?: ""
            val maybeSource = source?.let { "source: { file: \"${it.file}\", row: ${it.row}, col: ${it.col} }" } ?: ""
            val maybeResolutionStack = resolutionStack?.let { "uriResolutionStack: $it" } ?: ""

            val errorCause = cause?.toString()
            val maybeCause = errorCause?.let { "\nThis exception was caused by the following exception:\n$errorCause" } ?: ""

            val maybeDelim = innerError?.let { "\nAnother exception was encountered during execution:\n$innerError" } ?: ""

            return listOfNotNull(
                reason,
                "code: $code",
                "uri: $uri",
                maybeMethod,
                maybeArgs,
                maybeSource,
                maybeResolutionStack,
                maybeCause,
                maybeDelim
            )
                .filter { it.isNotEmpty() }
                .joinToString("\n")
        }
    }
}
