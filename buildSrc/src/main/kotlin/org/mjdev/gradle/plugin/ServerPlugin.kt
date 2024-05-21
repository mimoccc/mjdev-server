package org.mjdev.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.mjdev.gradle.base.BasePlugin
import org.mjdev.gradle.tasks.InstallTask
import org.mjdev.gradle.tasks.StartTask
import org.mjdev.gradle.tasks.UninstallTask
import org.mjdev.gradle.tasks.StopTask
import org.mjdev.gradle.tasks.RestartTask
import org.mjdev.gradle.tasks.CreateDebTask
import org.mjdev.gradle.tasks.CleanProjectTask
import org.mjdev.gradle.tasks.CreatePropsTask
import org.mjdev.gradle.plugin.config.ServerConfig
import org.mjdev.gradle.extensions.ProjectExt.libs
import org.mjdev.gradle.extensions.ProjectExt.apply
import org.mjdev.gradle.extensions.DependencyExt.implementation
import org.mjdev.gradle.extensions.ProjectExt.extension
import org.mjdev.gradle.extensions.ProjectExt.register
import org.mjdev.gradle.extensions.ProjectExt.cleanTask
import org.mjdev.gradle.extensions.ProjectExt.fromBuildPropertiesFile
import org.mjdev.gradle.extensions.ProjectExt.runningFromAndroidStudio

class ServerPlugin : BasePlugin() {
    private val isDebug
        get() = project.runningFromAndroidStudio

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
        afterEvaluate {
            register<CreatePropsTask>() {
                propsFilePath = ServerConfig.configPropertiesFile
                propsClass = ServerConfig::class.java
                mustRunAfter(cleanTask())
            }
            configure<JavaApplication>() {
                mainClass.set(serverConfig.mainClassName)
                applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDebug")
                applicationName = serverConfig.applicationName
            }
//            configure<ScriptingExtension> {}
//            configure<JibExtension> {}
//            configure<KtorExtension> {}
//            configure<KotlinJvmProjectExtension> {}
            configure<SourceSetContainer> {
                named("main") {
                    java.srcDir("src/main/kotlin")
                    resources.srcDir("src/main/resources")
                }
            }
            configure<DistributionContainer> {
            }
        }
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
            implementation(libs.ktor.server.status.pages)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.network.tls.certificates)
            implementation(libs.ktor.server.partial.content)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.client.okhttp)
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
            implementation(libs.acme4j)
//            implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:1.6.20-RC2")
        }
    }
}