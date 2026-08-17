// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
  //includeBuild("../dependency-analysis-gradle-plugin") // uncomment this line and point to actual DAGP clone

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version "4.2.2"
  id("com.autonomousapps.build-health") version "3.18.0"
  id("org.jetbrains.kotlin.jvm") version "2.4.10" apply false
  id("com.autonomousapps.testkit") version "0.19" apply false
  id("com.gradleup.tapmoc") version "0.4.0" apply false
  id("com.vanniktech.maven.publish") version "0.37.0" apply false
  id("com.gradle.plugin-publish") version "2.1.1" apply false
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    mavenCentral()
    exclusiveContent {
      filter {
        includeGroup("com.android")
        includeGroup("com.android.tools.build")
      }
      forRepository {
        google()
      }
    }
  }
}

develocity {
  val isCI = providers.environmentVariable("CI").isPresent
  val isEnabled = providers.gradleProperty("autonomousapps.scans.publish").getOrElse("false").toBoolean()

  buildScan {
    publishing.onlyIf { isCI || isEnabled }

    tag(if (isCI) "CI" else "local")

    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"
  }
}

rootProject.name = "dedebug"

include(":plugin")
