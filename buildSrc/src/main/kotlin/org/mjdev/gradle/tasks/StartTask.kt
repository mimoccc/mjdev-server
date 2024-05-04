package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask

open class StartTask : BaseTask() {
    override fun init() {
        group = "mjdev"
        description = "Start server and components."
    }

    override fun start() {
    }

    override fun finish() {
    }
}