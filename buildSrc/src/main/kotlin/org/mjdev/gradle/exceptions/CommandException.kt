package org.mjdev.gradle.exceptions

@Suppress("unused")
open class CommandException(
    message: String,
    cause: Throwable? = null,
    val isFatal: Boolean = false
) : Exception(message, cause) {
    override val message: String
        get() = StringBuilder().apply {
            append(super.message)
            var problem = cause
            while (problem != null) {
                append("\n")
                append(problem.message)
                problem = problem.cause
            }
        }.toString()

    constructor(
        cause: Throwable
    ) : this(cause.message ?: "", cause)
}
