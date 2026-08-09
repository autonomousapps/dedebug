// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestBuildType
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.autonomousapps.dedebug.DeDebugSettingsPlugin.Companion.findRequestsTests
import com.autonomousapps.dedebug.DeDebugSettingsPlugin.Companion.isExcluded
import com.autonomousapps.dedebug.internal.Plugins
import com.autonomousapps.dedebug.internal.utils.string.capitalize
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * This project plugin deliberately doesn't have an ID. It's meant as an internal implementation detail. It's how
 * [DeDebugSettingsPlugin] configures projects throughout the build.
 */
public abstract class DeDebugPlugin : Plugin<Project> {

  private companion object {
    const val DEBUG = "debug"
    const val RELEASE = "release"
  }

  override fun apply(target: Project): Unit = target.run {
    configureAll()

    pluginManager.withPlugin(Plugins.ANDROID_APP) {
      configureApp()
    }
    pluginManager.withPlugin(Plugins.ANDROID_TEST) {
      configureTest()
    }
    pluginManager.withPlugin(Plugins.ANDROID_LIB) {
      configureLib()
    }
  }

  /**
   * Since [DeDebugSettingsPlugin] unconditionally modifies the task execution requests, we also need to
   * unconditionally configure tests globally.
   */
  private fun Project.configureAll() {
    tasks.withType(Test::class.java).configureEach { t ->
      findRequestsTests()?.let { requestedTests ->
        // This is the `@Option(option = "tests")`, i.e., `--tests=...`
        t.setTestNameIncludePatterns(requestedTests)
      }
    }
  }

  /** For `com.android.application` projects. */
  private fun Project.configureApp() {
    setMatchingFallbacks()
  }

  /** For `com.android.test` projects. */
  private fun Project.configureTest() {
    setMatchingFallbacks()
  }

  /** For `com.android.library` projects. */
  private fun Project.configureLib() {
    if (isExcluded()) {
      setMatchingFallbacks()
      return
    }

    // Change the tested build type form its default ("debug") to "release".
    extensions.configure(LibraryExtension::class.java) { lib ->
      lib.testBuildType = RELEASE
    }

    extensions.configure(LibraryAndroidComponentsExtension::class.java) { lib ->
      // Disable the "debug" build type.
      lib.beforeVariants(lib.selector().withBuildType(DEBUG)) { builder ->
        builder.enable = false

        // Shim tests are e.g. `testDebugUnitTest` -> `testReleaseUnitTest`. Helps with developer muscle memory.
        registerShimUnitTest(builder.name)
        registerShimConnectedTest(builder.name)
      }

      // Mark the "release" build type debuggable, i.e., adds `android:debuggable` to the test APK's manifest. Some
      // tools (e.g., mockk) require this.
      lib.finalizeDsl { lib ->
        lib.buildTypes.configureEach { buildType ->
          if (buildType.name == RELEASE) {
            (buildType as TestBuildType).isDebuggable = true
          }
        }
      }
    }
  }

  private fun Project.setMatchingFallbacks() {
    extensions.configure(CommonExtension::class.java) { e ->
      e.buildTypes.run {
        named(DEBUG) {
          it.matchingFallbacks += RELEASE
        }
      }
    }
  }

  /**
   * To support migration from `testDebugUnitTest` to `test`, recreate the `testDebugUnitTest` task and set it to
   * depend on `testReleaseUnitTest`. Emit a warning when used.
   */
  private fun Project.registerShimUnitTest(variantName: String) {
    val taskName = "test${variantName.capitalize()}UnitTest"
    val preferredTask = taskName.replace(DEBUG.capitalize(), RELEASE.capitalize())
    
    tasks.register(taskName) { t ->
      t.group = LifecycleBasePlugin.VERIFICATION_GROUP
      t.description = "(Deprecated: use 'test' instead) Run unit tests."

      // Run the real test task
      t.dependsOn(preferredTask)

      t.doLast {
        logger.warn("The task '${name}' is deprecated. Use 'test' instead.")
      }
    }
  }

  /**
   * To support migration from `connectedDebugAndroidTest` to `connectedAndroidTest`, recreate the
   * `connectedDebugAndroidTest` task and set it to depend on `connectedReleaseAndroidTest`. Emit a warning when used.
   */
  private fun Project.registerShimConnectedTest(variantName: String) {
    val taskName = "connected${variantName.capitalize()}AndroidTest"
    val preferredTask = taskName.replace(DEBUG.capitalize(), RELEASE.capitalize())

    project.tasks.register(taskName) { t ->
      t.group = LifecycleBasePlugin.VERIFICATION_GROUP
      t.description =
        "(Deprecated: use 'connectedAndroidTest' instead) Installs and runs instrumentation tests on connected devices."

      // Run the real test task
      t.dependsOn(preferredTask)

      t.doLast {
        logger.warn("The task '${name}' is deprecated. Use 'connectedAndroidTest' instead.")
      }
    }
  }
}
