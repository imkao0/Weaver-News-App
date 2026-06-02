package dev.mkao.weaver.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    targetSdk = 36
                    versionCode = 1
                    versionName = "1.0"
                    vectorDrawables {
                        useSupportLibrary = true
                    }
                }

                buildFeatures {
                    buildConfig = true
                }

                testOptions {
                    unitTests.isReturnDefaultValues = true
                    unitTests.isIncludeAndroidResources = true
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                        excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
                        excludes += "/META-INF/versions/9/module-info.class"
                        excludes += "/META-INF/DEPENDENCIES"
                        excludes += "/META-INF/{INDEX.LIST,NOTICE,LICENSE,NOTICE.txt,LICENSE.txt}"
                    }
                }
            }
        }
    }
}
