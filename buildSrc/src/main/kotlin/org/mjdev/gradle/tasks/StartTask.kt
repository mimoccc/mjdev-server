package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.data.ServiceFile

open class StartTask : BaseTask() {
    override fun init() {
        group = "mjdev"
        description = "Start server and components."
        finalizedBy("run")
    }

    override fun start() {
        val services = rootDir.resolve(serverConfig.servicesDir)
        val servicesExists = services.exists()
        if (!servicesExists) {
            return
        }
        val parsedServices = parseServices(services)
        if (parsedServices.isEmpty()) {
            return
        }
        parsedServices.forEach { service ->
            if (service.isEnabled) {
                if (isDebug) {
                    println("Note: hosts can be edited only when not debugging.")
                    println("Service: [${service.serviceHostName}], not added.")
                } else {
                    hostsCmd.add(service.serviceName, "127.0.0.1")
                }
                when {
                    service.isProxy -> {
                        println("Service: ${service.serviceName}, is proxy ...")
                    }

                    service.isNodeJs -> {
                        println("Service: ${service.serviceName}, is nodejs ...")
                        checkInstalled(service)
                        nodeCmd.startService(service.serviceDir)
                    }

                    service.isAndroid -> {
                        println("Service: ${service.serviceName}, is android ...")
                        checkInstalled(service)
                        // todo build
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun checkInstalled(service: ServiceFile) {
        if (service.isInstalled) return
        println("Service: ${service.serviceName}, installing ...")
        when {
            service.isStatic -> {
                println("Service: ${service.serviceName}, is static ...")
            }

            service.isGit -> {
                println("Service: ${service.serviceName}, git clone ...")
                println("Service git repo: ${service.gitUrl}")
                gitCmd.clone(service.gitUrl, service.serviceDir)
            }

//            service.isZip -> {
//                println("Service: ${service.serviceName}, from zip ...")
//                println("Service git repo: ${service.gitUrl}")
//                zipCmd.unzip(service.zipFile, service.serviceDir)
//            }

            else -> {
                println("Service: ${service.serviceName}, is unknown type ${service.serviceType} ...")
            }
        }
    }

    override fun finish() {
    }
}