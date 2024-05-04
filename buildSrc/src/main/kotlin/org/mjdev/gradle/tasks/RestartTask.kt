package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask

open class RestartTask : BaseTask() {
    override fun init() {
        group = "mjdev"
        description = "Restart server and components."
    }

    override fun start() {
    }

    override fun finish() {
    }
}