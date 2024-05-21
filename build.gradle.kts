repositories {
    mavenCentral()
    google()
}

plugins {
    id("ServerPlugin")
}

server {
    mainClassName = "org.mjdev.server.MainKt"
    servicesDir = "services"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    @Suppress("DEPRECATION")
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.gradle.api)
        classpath(libs.gradle.kotlin.plugin)
        classpath(libs.gradle.ktor.plugin)
    }
}
