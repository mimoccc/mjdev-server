package org.mjdev.gradle.base

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Modifier
import java.util.Locale
import java.util.Properties

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class BasePlugin : Plugin<Project> {
    lateinit var project: Project

    override fun apply(project: Project) {
        this.project = project
        project.doInit()
    }

    abstract fun Project.doInit()

}
