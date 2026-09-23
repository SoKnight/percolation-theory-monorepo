plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "me.soknight.university"
version = "1.0-SNAPSHOT"

application {
    mainClass = "MainKt"

    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
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
    implementation(libs.clikt)
    implementation(libs.lets.plot.kotlin)
    runtimeOnly(libs.slf4j.nop)
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
