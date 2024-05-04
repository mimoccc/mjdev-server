package org.mjdev.gradle.plugin

import org.gradle.api.Project
import org.mjdev.gradle.base.BasePlugin
import org.mjdev.gradle.tasks.InstallTask
import org.mjdev.gradle.tasks.StartTask
import org.mjdev.gradle.tasks.UninstallTask
import org.mjdev.gradle.tasks.StopTask
import org.mjdev.gradle.tasks.RestartTask
import org.mjdev.gradle.tasks.CreateDebTask
import org.mjdev.gradle.tasks.CleanTask

class ServerPlugin : BasePlugin() {
    override fun Project.doInit() {
        register<CleanTask>()
        register<InstallTask>()
        register<UninstallTask>()
        register<StartTask>()
        register<StopTask>()
        register<RestartTask>()
        register<CreateDebTask>()
    }
}