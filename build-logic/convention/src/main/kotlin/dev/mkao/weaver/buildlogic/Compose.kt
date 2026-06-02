package dev.mkao.weaver.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.apply {
        buildFeatures {
            compose = true
        }
    }

    dependencies {
        val bom = libs.findLibrary("compose.bom").get()
        "implementation"(platform(bom))
        "implementation"(libs.findLibrary("compose.ui").get())
        "implementation"(libs.findLibrary("compose.ui.graphics").get())
        "implementation"(libs.findLibrary("compose.ui.tooling.preview").get())
        "implementation"(libs.findLibrary("compose.material3").get())
        "debugImplementation"(libs.findLibrary("compose.ui.tooling").get())
        "debugImplementation"(libs.findLibrary("compose.ui.test.manifest").get())
        "androidTestImplementation"(platform(bom))
    }
}
