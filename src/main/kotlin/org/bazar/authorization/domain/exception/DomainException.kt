package org.bazar.authorization.domain.exception

class DomainException(
    val exceptionType: DomainErrors,
    vararg params: Any,
    cause: Exception? = null
) : RuntimeException(formatMessage(exceptionType, params), cause) {

    companion object {
        fun formatMessage(type: DomainErrors, args: Array<out Any>): String {
            return if (args.isEmpty()) {
                type.message
            } else {
                try {
                    String.format(type.message, *args)
                } catch (e: Exception) {
                    "${type.message} [Args: ${args.joinToString()}]"
                }
            }
        }
    }
}

enum class DomainErrors(val message: String) {
    ILLEGAL_ARGUMENT("Illegal Argument: %s"),
    NO_SUCH_USER_IN_SPACE("No such user found in space: %s"),
    NO_SUCH_ROLE("No such role: %s"),
    INSUFFICIENT_PERMISSIONS("Insufficient permissions for this action"),
    USER_ALREADY_EXISTS("User already exists in space"),
    NO_SUCH_ATTRIBUTE("No such attribute: %s"),
    UNAUTHENTICATED("Unauthenticated user")
}
