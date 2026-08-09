// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug

import com.autonomousapps.dedebug.extension.DeDebugExtension
import com.autonomousapps.dedebug.internal.ProjectFilter
import com.autonomousapps.dedebug.internal.TaskRequestMapper
import com.autonomousapps.dedebug.internal.utils.extensionaware.extraProperties
import com.autonomousapps.dedebug.internal.utils.extensionaware.get
import com.autonomousapps.dedebug.internal.utils.extensionaware.getOrNull
import org.gradle.api.IsolatedAction
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty

/**
 * ```
 * plugins {
 *   id("com.autonomousapps.dedebug")
 * }
 * ```
 */
@Suppress("UnstableApiUsage")
public abstract class DeDebugSettingsPlugin : Plugin<Settings> {

  internal companion object {
    private const val OPTION_TESTS: String = "com.autonomousapps.dedebug.tests"
    private const val EXCLUDES: String = "com.autonomousapps.dedebug.excludes"
    private const val INCLUDES: String = "com.autonomousapps.dedebug.includes"

    /**
     * Returns the list of tests requested by the user, or null. Never an empty list.
     *
     * @see <a href="https://docs.gradle.org/current/userguide/java_testing.html">Testing in Java & JVM projects</a>
     */
    fun Project.findRequestsTests(): List<String>? {
      return extraProperties.getOrNull<String>(OPTION_TESTS)?.split(',')
    }

    /** Returns `true` if Android library is excluded from single-variant behavior. */
    fun Project.isExcluded(): Boolean {
      val excludes = extraProperties.get<Provider<Set<String>>>(EXCLUDES).get()
      val includes = extraProperties.get<Provider<Set<String>>>(INCLUDES).get()

      if (excludes.isNotEmpty()) {
        logger.info("Excluding ${excludes.size} projects: $excludes.")
      }
      if (includes.isNotEmpty()) {
        logger.info("Including ${includes.size} projects: $includes.")
      }

      val filter = ProjectFilter(
        projectPath = path,
        excludes = excludes,
        includes = includes,
      )

      return filter.isExcluded()
    }
  }

  override fun apply(target: Settings): Unit = target.run {
    val extension = DeDebugExtension.create(this)

    // Apply project plugin globally
    gradle.lifecycle.beforeProject(
      ConfigureProject(
        excludes = extension.excludes,
        includes = extension.includes,
      )
    )

    // Handle `--tests` options
    configureTestRequests()
  }

  /**
   * To support single-variant Android libraries, we map the `--tests ...` option to a custom property for configuring
   * the "real" test task in each project. This enables developers to call
   *
   * ```
   * ./gradlew <android-library>:test --tests ...
   * ```
   *
   * and have it "just work," even though Android's `test` task is just a lifecycle task without a `--tests` option.
   *
   * Note that a complex Gradle invocation looks like
   *
   * ```
   * [
   *   DefaultTaskExecutionRequest{
   *     args=[foo:bar:testRUT, --tests, FooBarTest, bar:baz:help],
   *     projectPath='null',
   *     rootDir='null',
   *   }
   * ]
   * ```
   *
   * Which is to say, it's only a single [TaskExecutionRequest][org.gradle.TaskExecutionRequest], with each part of the
   * CLI its own "arg." I don't know why it's wrapped in an iterable.
   *
   * @see <a href="https://docs.gradle.org/current/userguide/java_testing.html">Testing in Java & JVM projects</a>
   * @see [findRequestsTests]
   */
  private fun Settings.configureTestRequests() {
    val sp = gradle.startParameter

    val mapper = TaskRequestMapper(sp.taskRequests)
    val newTaskRequests = mapper.mappedRequests
    val requestedTests = mapper.requestedTests

    sp.setTaskRequests(newTaskRequests)

    // Tests can be passed multiple `--tests ...` options, so we handle that.
    if (requestedTests.isNotEmpty()) {
      gradle.lifecycle.beforeProject(TestsOption(requestedTests.joinToString(separator = ",")))
    }
  }

  private class ConfigureProject(
    private val excludes: SetProperty<String>,
    private val includes: SetProperty<String>,
  ) : IsolatedAction<Project> {
    override fun execute(target: Project): Unit = target.run {
      target.extraProperties.set(EXCLUDES, excludes)
      target.extraProperties.set(INCLUDES, includes)
      pluginManager.apply(DeDebugPlugin::class.java)
    }
  }

  private class TestsOption(
    /**
     * Comma-delimited requests. This:
     * ```
     * --tests Foo --tests Bar
     * ```
     *
     * Becomes:
     * ```
     * "Foo,Bar"
     * ```
     */
    private val requestedTests: String
  ) : IsolatedAction<Project> {
    override fun execute(target: Project) {
      target.extraProperties.set(OPTION_TESTS, requestedTests)
    }
  }
}
