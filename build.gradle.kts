plugins {
    alias(libs.plugins.paperweight)
}

group = "me.hugo.thankmas"
version = "1.0-SNAPSHOT"

dependencies {
    paperweight.paperDevBundle(libs.versions.paper)

    // LuckPerms Plugin API.
    compileOnly(libs.luck.perms)

    // Citizens Plugin API.
    compileOnly(libs.citizens) {
        exclude(mutableMapOf("group" to "*", "module" to "*"))
    }

    // Advanced Slime World Manager Plugin API.
    compileOnly(libs.aswm)
    compileOnly(libs.aswm.loaders)

    compileOnly(files("C:/Users/hugov/IdeaProjects/thankmas/common-paper/libs/PaperPolar.jar"))

    // Dependency Injection framework.
    ksp(libs.koin.ksp.compiler)

    // Main Thankmas framework.
    api(project(":common"))

    // Minecraft UI interface framework from the absolute goats at Noxcrew.
    api(libs.interfaces)

    // Nice command framework.
    api(libs.lamp.common)
    api(libs.lamp.bukkit)

    // Nice scoreboard API.
    api(libs.fastboard)

    // Library to access minecraft world files.
    api(libs.hephaistos)

    api(libs.bundles.exposed.runtime)
    api(libs.exposed.jbdc)

    // Amazon S3 API for nice world downloading and uploading :)
    api(platform("software.amazon.awssdk:bom:2.26.25"))
    api("software.amazon.awssdk:s3")
    api("software.amazon.awssdk:sso")
    api("software.amazon.awssdk:ssooidc")
    api("software.amazon.awssdk:apache-client")

    // Zip library
    api("org.zeroturnaround:zt-zip:1.17")
}