pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://mirrors.huaweicloud.com/repository/maven/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.6"
}

// 根工程即 Stonecutter 的共享源码工程；其源码目录重定向到 common/，保持“common 是唯一共享源”的结构。
// Stonecutter 据此生成 versions/1.20.1-forge 与 versions/1.21.8-neoforge 两个版本工程。
stonecutter {
    create(rootProject) {
        fun match(version: String, loader: String) =
            version("$version-$loader", version).buildscript("build.$loader.gradle.kts")
        match("1.20.1", "forge")
        match("1.21.8", "neoforge")
        vcsVersion = "1.20.1-forge"
    }
}

rootProject.name = "fku"
