package org.mjdev.gradle.plugin.config

import java.lang.reflect.Modifier

interface IConfig {
    fun toMap(): Map<*, *>
}

inline fun <reified T : Any> IConfig.toHashMap(): Map<Any, Any> = mutableMapOf<Any, Any>().apply {
    val fields = T::class.java.declaredFields.filter {
        !Modifier.isStatic(it.modifiers)
    }.onEach {
        it.trySetAccessible()
    }
    fields.forEach { field ->
        if (field.name.lowercase() != "companion") {
            val key = field.name
            val value = field.get(this@toHashMap)
            put(key, value)
        }
    }
}
