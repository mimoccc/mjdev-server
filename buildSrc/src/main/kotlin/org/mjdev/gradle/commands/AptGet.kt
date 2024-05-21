package org.mjdev.gradle.commands

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command

@Suppress("unused")
class AptGet(task: BaseTask) : Command(task) {
    fun update() = commands {
        sudo("apt-get update")
    }

    fun upgrade() = commands {
        sudo("apt-get upgrade")
    }

    fun install(pkg: String) = commands {
        sudo("apt-get install -y $pkg")
    }
}
