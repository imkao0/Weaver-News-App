plugins {
    `kotlin-dsl`
}

group = "dev.mkao.weaver.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.baselineprofile.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "weaver.android.application"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "weaver.android.application.compose"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplicationFirebase") {
            id = "weaver.android.application.firebase"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidApplicationFirebaseConventionPlugin"
        }
        register("androidLibrary") {
            id = "weaver.android.library"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "weaver.android.library.compose"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "weaver.android.hilt"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "weaver.android.room"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidRoomConventionPlugin"
        }
        register("androidTest") {
            id = "weaver.android.test"
            implementationClass = "dev.mkao.weaver.buildlogic.AndroidTestConventionPlugin"
        }
    }
}
