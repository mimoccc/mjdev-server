plugins {
    id("ServerPlugin")
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
    }
}