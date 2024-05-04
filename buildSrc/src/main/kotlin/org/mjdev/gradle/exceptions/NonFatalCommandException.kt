package org.mjdev.gradle.exceptions

@Suppress("unused")
class NonFatalCommandException(
    message: String,
    cause: Throwable? = null
) : CommandException(message, cause, false) {
    constructor(
        cause: Throwable
    ) : this(cause.message ?: "", cause)
}
