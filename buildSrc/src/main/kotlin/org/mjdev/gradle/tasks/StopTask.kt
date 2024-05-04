package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask

open class StopTask : BaseTask() {
    override fun init() {
        group = "mjdev"
        description = "Stop server and components."
    }

    override fun start() {
    }

    override fun finish() {
    }
}