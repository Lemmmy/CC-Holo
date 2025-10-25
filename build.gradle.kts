import java.text.SimpleDateFormat
import java.util.*

plugins {
  java
  idea
  `maven-publish`
  alias(libs.plugins.neoforged.legacyforge)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.modrinth.minotaur)
}

val modId: String by project
val modVersion: String by project

group = "sh.lem"
version = modVersion

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
  }
}

repositories {
  mavenCentral()

  maven("https://maven.minecraftforge.net")
  maven("https://thedarkcolour.github.io/KotlinForForge/") {
    content {
      includeGroup("thedarkcolour")
    }
  }

  maven("https://maven.squiddev.cc/") {
    content {
      includeGroup("cc.tweaked")
    }
  }
}

legacyForge {
  version = "${libs.versions.minecraft.get()}-${libs.versions.forge.get()}"
  validateAccessTransformers = true

  parchment {
    minecraftVersion = libs.versions.minecraft.get()
    mappingsVersion = libs.versions.parchment.get()
  }

  runs {
    register("client") {
      client()
    }
    create("server") {
      server()
      programArgument("--nogui")
    }
    create("data") {
      data()
      programArguments.addAll("--mod", modId)
      programArguments.addAll("--all")
      programArguments.addAll("--output", file("src/generated/resources/").absolutePath)
      programArguments.addAll("--existing", file("src/main/resources/").absolutePath)
    }
  }

  mods {
    register(modId) {
      sourceSet(sourceSets.main.get())
    }
  }
}

sourceSets.main {
  resources.srcDir("src/generated/resources")
}

dependencies {
  implementation(libs.kotlinforforge)

  // modCompileOnly(libs.cc.core)
  modCompileOnly(libs.cc.api.core)
  modCompileOnly(libs.cc.api.forge)
  modRuntimeOnly(libs.cc.runtime)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.java.get()
}

tasks.processResources {
  inputs.property("version", project.version)
  inputs.property("forgeVersion", libs.versions.forge)

  var props = mapOf(
    "forgeVersion" to libs.versions.forge.get(),
    "minecraftVersion" to libs.versions.minecraft.get(),
    "ccTweakedVersion" to libs.versions.cc.get(),
    "file" to mapOf("jarVersion" to project.version),
  )

  filesMatching("META-INF/mods.toml") {
    expand(props)
  }
}

// Ensure the produced jar has proper metadata
tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Specification-Title" to project.name,
        "Specification-Vendor" to "lem",
        "Specification-Version" to project.version,
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to "lem",
        "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd").format(Date())
      )
    )
  }

  // Ship the licenses with the mod
  from("LICENSES") {
    into("META-INF/LICENSES")
  }
}

(findProperty("modrinthApiKey") as? String?)?.let { modrinthKey ->
  modrinth {
    token.set(modrinthKey)
    projectId.set("DuhavUpy")
    versionNumber.set("${libs.versions.minecraft.get()}-$modVersion")
    versionName.set(modVersion)
    versionType.set("release")
    uploadFile.set(tasks.jar)
    changelog.set("Release notes can be found on the [GitHub repository](https://github.com/Lemmmy/CC-Holo/commits/${libs.versions.minecraft.get()}).")
    gameVersions.add(libs.versions.minecraft.get())
    loaders.add("forge")

    syncBodyFrom.set(provider { file("README.md").readText() })

    dependencies {
      required.project("cc-tweaked")
    }
  }

  tasks.modrinth { dependsOn(tasks.modrinthSyncBody) }
  tasks.publish { dependsOn(tasks.modrinth) }
}

val mavenUsername: String? = System.getenv("MAVEN_USERNAME")
val mavenPassword: String? = System.getenv("MAVEN_PASSWORD")
if (mavenUsername != null && mavenPassword != null) {
  publishing {
    publications {
      register("mavenJava", MavenPublication::class) {
        from(components["java"])
      }
    }

    repositories {
      maven {
        name = "lemmmyRepo"
        url = uri("https://repo.lem.sh/releases")

        if (!System.getenv("MAVEN_USERNAME").isNullOrEmpty()) {
          credentials {
            username = System.getenv("MAVEN_USERNAME")
            password = System.getenv("MAVEN_PASSWORD")
          }
        } else {
          credentials(PasswordCredentials::class)
        }

        authentication {
          create<BasicAuthentication>("basic")
        }
      }
    }
  }
}
