plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0" apply false
    id("io.papermc.paperweight.core") version "1.2.0"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }
}

val paperMavenPublicUrl = "https://papermc.io/repo/repository/maven-public/"

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }
}

val spigotDecompiler: Configuration by configurations.creating

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content {
            onlyForConfigurations(
                configurations.paperclip.name,
                spigotDecompiler.name,
            )
        }
    }
}

dependencies {
    paramMappings("net.fabricmc:yarn:1.17.1+build.65:mergedv2")
    remapper("net.fabricmc:tiny-remapper:0.6.0:fat")
    decompiler("net.minecraftforge:forgeflower:1.5.498.12")
    spigotDecompiler("io.papermc:patched-spigot-fernflower:0.1+build.4")
    paperclip("io.papermc:paperclip:2.0.1")
}

paperweight {
    minecraftVersion.set(providers.gradleProperty("mcVersion"))
    serverProject.set(project(":Paper-Server"))

    paramMappingsRepo.set(paperMavenPublicUrl)
    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    craftBukkit {
        fernFlowerJar.set(layout.file(spigotDecompiler.elements.map { it.single().asFile }))
    }

    paper {
        spigotApiPatchDir.set(layout.projectDirectory.dir("patches/api"))
        spigotServerPatchDir.set(layout.projectDirectory.dir("patches/server"))

        mappingsPatch.set(layout.projectDirectory.file("build-data/mappings-patch.tiny"))
        reobfMappingsPatch.set(layout.projectDirectory.file("build-data/reobf-mappings-patch.tiny"))

        additionalSpigotMemberMappings.set(layout.projectDirectory.file("build-data/additional-spigot-member-mappings.csrg"))
        craftBukkitPatchPatchesDir.set(layout.projectDirectory.dir("build-data/craftbukkit-patch-patches"))

        reobfPackagesToFix.addAll(
            "co.aikar.timings",
            "com.destroystokyo.paper",
            "com.mojang",
            "io.papermc.paper",
            "net.kyori.adventure.bossbar",
            "net.minecraft",
            "org.bukkit.craftbukkit",
            "org.spigotmc"
        )
    }
}

tasks.generateDevelopmentBundle {
    apiCoordinates.set("io.papermc.paper:paper-api")
    mojangApiCoordinates.set("io.papermc.paper:paper-mojangapi")
    libraryRepositories.addAll(
        "https://repo.maven.apache.org/maven2/",
        paperMavenPublicUrl,
    )
}

publishing {
    if (project.providers.gradleProperty("publishDevBundle").forUseAtConfigurationTime().isPresent) {
        publications.create<MavenPublication>("devBundle") {
            artifact(tasks.generateDevelopmentBundle) {
                artifactId = "dev-bundle"
            }
        }
    }
}

allprojects {
    publishing {
        repositories {
            maven("https://papermc.io/repo/repository/maven-snapshots/") {
                name = "paperSnapshots"
                credentials(PasswordCredentials::class)
            }
        }
    }
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}
