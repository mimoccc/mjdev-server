package org.mjdev.gradle.plugin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.mjdev.gradle.base.BasePlugin
import org.mjdev.gradle.tasks.InstallTask
import org.mjdev.gradle.tasks.StartTask
import org.mjdev.gradle.tasks.UninstallTask
import org.mjdev.gradle.tasks.StopTask
import org.mjdev.gradle.tasks.RestartTask
import org.mjdev.gradle.tasks.CreateDebTask
import org.mjdev.gradle.tasks.CleanProjectTask
import org.mjdev.gradle.plugin.config.ServerConfig
import org.mjdev.gradle.extensions.ProjectExt.libs
import org.mjdev.gradle.extensions.ProjectExt.apply
import org.mjdev.gradle.extensions.DependencyExt.implementation
import org.mjdev.gradle.extensions.ProjectExt.extension
import org.mjdev.gradle.extensions.ProjectExt.register
import org.mjdev.gradle.extensions.ProjectExt.property
import org.mjdev.gradle.extensions.ProjectExt.fromBuildPropertiesFile

class ServerPlugin : BasePlugin() {
    override fun Project.doInit() {
        val serverConfig = extension<ServerConfig>(ServerConfig.configName)
        fromBuildPropertiesFile(serverConfig, ServerConfig.configPropertiesFile)
        register<CleanProjectTask>()
        register<InstallTask>()
        register<UninstallTask>()
        register<StartTask>()
        register<StopTask>()
        register<RestartTask>()
        register<CreateDebTask>()
        apply(libs.plugins.kotlin.jvm)
        apply(libs.plugins.ktor)
        property("mainClassName", serverConfig.mainClassName)
        dependencies {
            implementation(libs.ktor.server.core.jvm)
            implementation(libs.ktor.server.netty.jvm)
            implementation(libs.ktor.server.default.headers)
            implementation(libs.ktor.server.compression)
            implementation(libs.ktor.server.caching.headers)
            implementation(libs.ktor.server.html.builder)
            implementation(libs.ktor.server.call.logging)
            implementation(libs.ktor.server.locations)
            implementation(libs.ktor.server.webjars)
            implementation(libs.ktor.server.forwarded.header)
            implementation(libs.ktor.server.http.redirect)
            implementation(libs.ktor.network.tls.certificates)
//            implementation("io.ktor:ktor-network:$ktor_version")
//            implementation("io.ktor:ktor-server-auth:$ktor_version")
//            implementation("io.ktor:ktor-server-auth:$ktor_version")
//            implementation("io.ktor:ktor-server-auth:$ktor_version")
//            implementation("io.ktor:ktor-server-auth:$ktor_version")
//            implementation("io.ktor:ktor-server-sessions:$ktor_version")
//            implementation("io.ktor:ktor-server-auth-ldap:$ktor_version")
//            implementation("io.ktor:ktor-server-auth:$ktor_version")
//            implementation("io.ktor:ktor-server-sessions:$ktor_version")
//            implementation("io.ktor:ktor-server-swagger:$ktor_version")
//            implementation("io.ktor:ktor-server-openapi:$ktor_version")
//            implementation("io.swagger.codegen.v3:swagger-codegen-generators:$swagger_codegen_version")
//            implementation("io.ktor:ktor-server-conditional-headers:$ktor_version")
//            implementation("io.ktor:ktor-server-cors:$ktor_version")
//            implementation("io.ktor:ktor-server-hsts:$ktor_version")
//            implementation("io.ktor:ktor-server-partial-content:$ktor_version")
//            implementation("io.ktor:ktor-server-data-conversion:$ktor_version")
//            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
//            implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktor_version")
//            implementation("io.ktor:ktor-serialization-kotlinx-cbor:$ktor_version")
//            implementation("io.ktor:ktor-serialization-kotlinx-protobuf:$ktor_version")
//            implementation("io.ktor:ktor-server-call-id:$ktor_version")
//            implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
//            implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")
//            implementation("io.ktor:ktor-server-metrics:$ktor_version")
//            implementation("io.dropwizard.metrics:metrics-jmx:$dropwizard_version")
            implementation(libs.kotlin.css)
            implementation(libs.logback.classic)
        }
    }
}