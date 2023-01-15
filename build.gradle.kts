import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.github.johnrengelman.shadow")
}

group = "net.stellarica.bot"
version = "0.1-SNAPSHOT"

repositories {
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
    // required for kord snapshot versions
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(libs.kord.extensions)
    implementation(libs.kotlin.stdlib)

    implementation(libs.jsoup)

    // Logging dependencies
    implementation(libs.logback)
    implementation(libs.logging)
}

application {
    mainClass.set("net.stellarica.bot.MainKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "net.stellarica.bot.MainKt"
        )
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
