@file:Suppress("unused")

package org.mjdev.server.dynu

import java.security.MessageDigest

fun String.md5(): String = hashString(this, "MD5")

fun String.sha256(): String = hashString(this, "SHA-256")

private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("") { str, it ->
            str + "%02x".format(it)
        }
}