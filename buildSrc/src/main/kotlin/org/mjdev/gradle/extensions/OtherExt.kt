package org.mjdev.gradle.extensions

import java.util.Locale

object  OtherExt {
    fun <T : Any> T.cast(type: Class<*>?): Any? = when (type?.simpleName?.lowercase()) {
        "string" -> this.toString()
        "int" -> parseInt(this)
        "long" -> parseLong(this)
        "boolean" -> parseBoolean(this)
        "double" -> parseDouble(this)
        else -> {
            println("> Cast missing for type: ${type?.simpleName}")
            null
        }
    }

    fun parseBoolean(value: Any): Boolean? = when (value) {
        is Boolean -> value
        is CharSequence ->
            when (value.toString().lowercase(Locale.US)) {
                "true" -> true
                "false" -> false
                else -> null
            }

        is Number ->
            when (value.toInt()) {
                0 -> false
                1 -> true
                else -> null
            }

        else -> null
    }

    fun parseInt(value: Any): Int? = when (value) {
        is Int -> value
        is CharSequence -> value.toString().toIntOrNull()
        is Number -> value.toInt()
        else -> null
    }

    fun parseLong(value: Any): Long? = when (value) {
        is Long -> value
        is CharSequence -> value.toString().toLongOrNull()
        is Number -> value.toLong()
        else -> null
    }

    fun parseDouble(value: Any): Double? = when (value) {
        is Double -> value
        is CharSequence -> value.toString().toDoubleOrNull()
        is Number -> value.toDouble()
        else -> null
    }

    fun <K, V> Map<K, V>.mapToString(
        separator: String = ": ",
        quoteFirst: Boolean = true,
        quoteSecond: Boolean = true,
        prefix: String = "\t",
        suffix: String = ",",
    ): String = map { kv ->
        val sdKey = kv.key.toString().replace("\"", "")
        val sdValue = kv.value.toString().replace("\"", "")
        val qf = if (quoteFirst) "\"" else ""
        val qs = if (quoteSecond) "\"" else ""
        "$prefix$qf$sdKey$qf$separator$qs$sdValue$qs"
    }.joinToString("$suffix\n") + "\n"
}