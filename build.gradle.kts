import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.github.johnrengelman.shadow")
}

group = "io.github.hydrazinemc"
version = "0.1-SNAPSHOT"

repositories {
    // You can remove this if you're not testing locally-installed KordEx builds
    mavenLocal()

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
    
    // required for kord snapshot versions
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(libs.kord.extensions)
    implementation(libs.kotlin.stdlib)

    implementation(libs.jsoup)

    // Database dependencies
    implementation(libs.kmongo)

    // Logging dependencies
    implementation(libs.logback)
    implementation(libs.logging)
}

application {
    mainClass.set("io.github.hydrazinemc.bot.HydrazineBotKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"

    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "io.github.hydrazinemc.bot.HydrazineBot"
        )
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
