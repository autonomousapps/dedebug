// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.fixtures

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.Dependency
import com.autonomousapps.kit.gradle.GradleProperties
import com.autonomousapps.kit.gradle.Plugin

internal abstract class AbstractFixture : AbstractGradleProject() {

  companion object {
    const val AGP_VERSION = "9.0.1"
    const val KGP_VERSION = "2.4.10"
    val AGP = Plugin("com.android.application", AGP_VERSION, apply = false)
    val KGP = Plugin("org.jetbrains.kotlin.jvm", KGP_VERSION, apply = false)

    val ANDROID_APP = Plugin("com.android.application")
    val ANDROID_LIB = Plugin("com.android.library")
    val ANDROID_TEST = Plugin("com.android.test")
    val DEBUG_AWAY = Plugin("com.autonomousapps.dedebug", PLUGIN_UNDER_TEST_VERSION)
    val JAVA_LIB = Plugin("java-library")
    val KOTLIN_JVM = Plugin("org.jetbrains.kotlin.jvm")

    const val COMPILE_SDK = 34

    val JUNIT_PLATFORM = Dependency.testImplementation("org.junit:junit-bom:5.14.4").onPlatform()
    val JUNIT_API = Dependency.testImplementation("org.junit.jupiter:junit-jupiter-api")
    val JUNIT_ENGINE = Dependency.testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    val JUNIT_LAUNCHER = Dependency.testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    val JUNIT4 = Dependency.testImplementation("junit:junit:4.13.2")
  }

  override fun newGradleProjectBuilder(): GradleProject.Builder {
    val properties = listOf(
      GradleProperties.BUILD_CACHE,
      GradleProperties.CONFIGURATION_CACHE_STABLE,
      GradleProperties.ISOLATED_PROJECTS_UNSTABLE,
      GradleProperties.PARALLEL,
    )

    return super.newGradleProjectBuilder()
      .withRootProject {
        gradleProperties += properties
        withSettingsScript {
          plugins(DEBUG_AWAY, AGP, KGP)
        }
      }
  }
}