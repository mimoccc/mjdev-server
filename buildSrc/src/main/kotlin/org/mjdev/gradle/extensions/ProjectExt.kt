package org.mjdev.gradle.extensions

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Provider
import org.gradle.internal.impldep.jakarta.xml.bind.DatatypeConverter
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Modifier
import java.util.Properties
import org.mjdev.gradle.extensions.OtherExt.cast

@Suppress("HasPlatformType", "unused", "MemberVisibilityCanBePrivate")
object ProjectExt {

    val Project.androidStudioVersion
        get() = project.extra.properties["android.studio.version"]

    val Project.runningFromAndroidStudio
        get() = DatatypeConverter.parseBoolean(project.extra.properties["android.injected.invoked.from.ide"].toString())

    val Project.isAndroidStudio
        get() = project.extra.properties.keys.contains("android.studio.version")

    val Project.libs
        get() = the<LibrariesForLibs>()

    inline fun <reified T> Project.extension(
        name: String? = null,
        config: T.() -> Unit = {}
    ): T = run {
        var cfg: T? = project.extensions.findByType(T::class.java)
        if (cfg == null && name != null) {
            project.extensions.create(name, T::class.java)
        }
        cfg = project.extensions.findByType(T::class.java)
        cfg?.apply {
            config(this)
        } as T
    }

    fun <T> Project.extension(cls: Class<T>): T =
        rootProject.extensions.findByType(cls)
            ?: project.extensions.findByType(cls)
            ?: throw (GradleException("Extension not configured : ${cls.simpleName}"))

    inline fun <reified T> Project.fromBuildPropertiesFile(
        config: T,
        configPropertiesFile: String
    ) {
        val props = project.loadBuildPropertiesFile(configPropertiesFile, false)
        val members = T::class.java.declaredFields.filter {
            !Modifier.isStatic(it.modifiers)
        }.onEach {
            it.trySetAccessible()
        }
        props.forEach { prop ->
            members.firstOrNull { field ->
                prop.key == field.name
            }?.apply {
                val value = prop.value.cast(this.type)
                try {
                    set(config, value)
                } catch (e: Exception) {
                    println("> Error set: $name[$type] = $value, ${e.message}")
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    fun Project.loadBuildPropertiesFile(
        path: String,
        exposeToExtra: Boolean = true
    ) = loadPropertiesFile(
        project.buildDir.resolve(path),
        if (exposeToExtra) project.extra else null
    )

    @Suppress("UnusedReceiverParameter")
    fun Project.loadPropertiesFile(
        propertiesFile: File,
        exposeToExtra: ExtraPropertiesExtension?
    ) = Properties().apply {
        try {
            load(FileInputStream(propertiesFile))
        } catch (e: Exception) {
            // ignored
        }
    }.apply {
        if (exposeToExtra != null) {
            forEach { prop ->
                exposeToExtra.set(prop.key.toString(), prop.value.toString())
            }
        }
    }

    inline fun <reified T : Task> Project.register(
        name: String? = null,
        block: T.() -> Unit = {}
    ): T {
        tasks.register(
            name ?: T::class.java.simpleName
                .replace("Task", "")
                .replaceFirstChar { c ->
                    c.lowercase()
                },
            T::class.java
        )
        val task = tasks.withType(T::class.java).first()
        block(task)
        return task
    }

    inline fun <reified T : Task> Project.task(scoped: T.() -> Unit = {}): T {
        val task = tasks.withType<T>().first()
        scoped(task)
        return task
    }

    inline fun <reified T : Task> Project.task(
        name: String,
        scoped: T.() -> Unit = {}
    ): T {
        val task = tasks.named(name).get() as T
        scoped(task)
        return task
    }

    fun Project.cleanTask(
        scoped: Task.() -> Unit = {}
    ) : Task = task<Task>("clean", scoped)

    fun Project.apply(plugin: Provider<*>) =
        project.plugins.apply(plugin.get().toString())

    fun Project.apply(plugin: String) =
        project.plugins.apply(plugin)

    fun Project.property(name: String, value: String) {
        project.setProperty(name, value)
    }

    inline fun <reified T : Plugin<Project>> Project.apply() =
        project.plugins.apply(T::class.java)
}