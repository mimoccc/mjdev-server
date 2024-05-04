package org.mjdev.gradle.base

import org.mjdev.gradle.exceptions.CommandException
import java.io.ByteArrayOutputStream

data class CommandStackEntry(
    val command: String,
    val onFail: (e: CommandException) -> Unit = {},
    val needSudo: Boolean = false,
    val errorOutput: ByteArrayOutputStream = ByteArrayOutputStream(),
    val output: ByteArrayOutputStream = ByteArrayOutputStream(),
)