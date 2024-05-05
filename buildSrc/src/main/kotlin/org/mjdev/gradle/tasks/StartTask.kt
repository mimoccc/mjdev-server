package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.data.Service
import org.mjdev.gradle.data.ServiceFile
import org.mjdev.gradle.plugin.config.ServerConfig
import java.io.File
import org.mjdev.gradle.extensions.ProjectExt.extension

open class StartTask : BaseTask() {

    private val config
        get() = project.extension<ServerConfig>()

    override fun init() {
        group = "mjdev"
        description = "Start server and components."
        finalizedBy("run")
    }

    override fun start() {
        val services = rootDir.resolve(config.servicesDir)
        if (!services.exists()) {
            logger.error("Services directory not found.")
            return
        }
        val parsedServices = parseServices(services)
        if (parsedServices.isEmpty()) {
            logger.error("No services found.")
            return
        }
        parsedServices.forEach { service ->
            when (service) {
                is Service -> {
                    // todo check integrity
                }

                is ServiceFile -> {
                    if(service.isDisabled) return@forEach
                    // todo check integrity & install if error || not exists
                    when {
                        service.exists -> {
                            // todo
                        }
                        else -> {
                            when {
                                service.isGit -> {
                                    gitCmd.clone(service.gitUrl, service.serviceDir)
                                }
//                                service.isZip -> {
//                                    zipCmd.unzip(service.zipFile, service.serviceDir)
//                                }
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
    }

    private fun parseServices(services: File) = services.listFiles()?.mapNotNull { file ->
        when {
            file.isDirectory -> Service(file)
            file.isFile -> ServiceFile(file)
            else -> null
        }
    } ?: emptyList()
}