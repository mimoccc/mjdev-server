package org.mjdev.gradle.base

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

abstract class BasePlugin : Plugin<Project> {
    private lateinit var project: Project

    override fun apply(project: Project) {
        this.project = project
        project.doInit()
    }

    abstract fun Project.doInit()

    inline fun <reified T : Task> Project.register(
        name: String? = null,
        block: T.() -> Unit = {}
    ): T {
        tasks.register(
            name ?: T::class.java.simpleName.replace("Task", "").lowercase(),
            T::class.java
        )
        val task = tasks.withType(T::class.java).first()
        block(task)
        return task
    }
}
