pluginManagement {
	plugins {
		// Update this in libs.version.toml when you change it here
		kotlin("jvm") version "1.8.0"
		kotlin("plugin.serialization") version "1.8.0"

		id("com.github.johnrengelman.shadow") version "8.1.1"
	}
}

rootProject.name = "StellaricaBot"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("libs.versions.toml"))
		}
	}
}
