plugins {
    kotlin("jvm") version "1.9.21"
    id("io.papermc.paperweight.userdev") version "1.5.11"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
}

group = "me.hugo.thankmas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven(url = "https://jitpack.io")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven(url = "https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven(url = "https://repo.rapture.pw/repository/maven-releases/")
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")

    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")
    implementation("fr.mrmicky:fastboard:2.1.2")
    implementation(files("C:/Users/hugov/IdeaProjects/TranslationsTest/build/libs/Thankmas-1.0-SNAPSHOT-all.jar"))

    implementation("com.github.Revxrsal.Lamp:common:3.1.9")
    implementation("com.github.Revxrsal.Lamp:bukkit:3.1.9")
    implementation("io.github.jglrxavpok.hephaistos:common:2.6.1")
    compileOnly("com.infernalsuite.aswm:api:1.20.4-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

tasks.shadowJar {
    relocate("fr.mrmicky.fastboard", "me.hugo.thankmas.fastboard")
}

tasks.compileKotlin {
    kotlinOptions.javaParameters = true
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}