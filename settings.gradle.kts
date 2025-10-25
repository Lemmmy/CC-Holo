pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()

    maven("https://maven.neoforged.net") {
      name = "NeoForge"
      content {
        includeGroup("net.neoforged")
      }
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "CC-Holo"
