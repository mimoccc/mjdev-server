package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask

open class UninstallTask : BaseTask() {
    override fun init() {
        group = "mjdev"
        description = "Uninstall server and components."
    }

    override fun start() {
    }

    override fun finish() {
    }
}