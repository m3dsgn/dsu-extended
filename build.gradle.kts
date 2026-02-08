plugins {
    id("com.android.application") version "9.1.0-alpha05" apply false
    id("com.android.library") version "9.1.0-alpha05" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
    id("com.google.dagger.hilt.android") version "2.59.1" apply false
    id("org.jmailen.kotlinter") version "5.4.0" apply false
}

val versionCode by extra { 11 }
val versionName by extra { "0.5-beta" }
val packageName by extra { "com.dsu.extended" }

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
