package org.mjdev.gradle.exceptions

@Suppress("unused")
class FatalCommandException(
    message: String,
    cause: Throwable? = null
) : CommandException(message, cause, true) {
    constructor(
        cause: Throwable
    ) : this(cause.message ?: "", cause)
}
