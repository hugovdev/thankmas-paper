plugins {
    kotlin("jvm") version "1.9.21"
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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")

    ksp("io.insert-koin:koin-ksp-compiler:1.3.0")
    implementation("fr.mrmicky:fastboard:2.0.2")
    implementation(files("C:/Users/hugov/IdeaProjects/TranslationsTest/build/libs/Thankmas-1.0-SNAPSHOT-all.jar"))

    implementation("com.github.Revxrsal.Lamp:common:3.1.8")
    implementation("com.github.Revxrsal.Lamp:bukkit:3.1.8")
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