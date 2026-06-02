package dev.mkao.weaver.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            extensions.findByType<LibraryExtension>()?.sourceSets
                ?.getByName("androidTest")?.assets?.srcDir("$projectDir/schemas")
            extensions.findByType<ApplicationExtension>()?.sourceSets
                ?.getByName("androidTest")?.assets?.srcDir("$projectDir/schemas")

            extensions.configure<KspExtension> {
                arg("room.schemaLocation", "$projectDir/schemas")
            }

            dependencies {
                "implementation"(libs.findLibrary("room.runtime").get())
                "implementation"(libs.findLibrary("room.ktx").get())
                "ksp"(libs.findLibrary("room.compiler").get())
            }
        }
    }
}
