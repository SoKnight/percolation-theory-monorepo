pluginManagement {
    // convention-плагины с общей логикой сборки заданий
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "percolation-theory-monorepo"

// практическая работа №1
include(":01-site-percolation:task1")
include(":01-site-percolation:task2")

// практическая работа №2
include(":02-percolation-systems:task1")
include(":02-percolation-systems:task2")
