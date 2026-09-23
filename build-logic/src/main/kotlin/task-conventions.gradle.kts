// Общая сборка для всех заданий
// Kotlin под Java 25, запуск через application, общие зависимости из каталога версий

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

group = "me.soknight.university"
version = "1.0-SNAPSHOT"

// в precompiled-плагине нет сгенерированного `libs`, каталог берётся по имени
val libs = versionCatalogs.named("libs")

application {
    // точка входа каждого задания:
    // функция main в файле Main.kt
    mainClass = "MainKt"

    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        // без этого на Windows вывод идёт в системной кодировке, и кириллица превращается в «?»
        "-Dstdout.encoding=UTF-8",
        "-Dstderr.encoding=UTF-8",
        "-Djava.awt.headless=true",
    )
}

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.findLibrary("clikt").get())
    implementation(libs.findLibrary("lets-plot-kotlin").get())
    runtimeOnly(libs.findLibrary("slf4j-nop").get())
}

tasks {
    named<JavaExec>("run") {
        // по умолчанию Gradle не передаёт программе ввод с консоли, и читать данные ей неоткуда
        standardInput = System.`in`
    }

    assembleDist {
        enabled = false
    }

    distTar {
        enabled = false
    }

    distZip {
        enabled = false
    }

    startScripts {
        enabled = false
    }
}
