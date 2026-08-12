// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.fixtures

import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.Source
import com.autonomousapps.kit.android.AndroidManifest
import com.autonomousapps.kit.gradle.Dependency.Companion.implementation
import com.autonomousapps.kit.gradle.android.AndroidBlock
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

internal class DeDebugFixture(
  private val excludes: Set<String> = emptySet(),
) : AbstractFixture() {

  fun build(): GradleProject {
    return newGradleProjectBuilder()
      .withRootProject {
        withSettingsScript {
          val exclusions = excludes.joinToString(separator = ", ") { "\"$it\"" }
          additions = """
            dedebug {
              exclude($exclusions)
            }
          """.trimIndent()
        }
      }
      .withAndroidSubproject("app") {
        sources = appSources
        manifest = AndroidManifest.simpleApp()
        withBuildScript {
          plugins(ANDROID_APP)
          android = AndroidBlock(
            namespace = "com.example.app",
            compileSdkVersion = COMPILE_SDK,
          )
          dependencies(
            // validating fallback support
            implementation(":features:one"),
            JUNIT4,
          )
        }
      }
      .withAndroidLibProject("features:one") {
        sources = libSources
        withBuildScript {
          plugins(ANDROID_LIB)
          android = AndroidBlock(
            namespace = "com.example.features.one",
            compileSdkVersion = COMPILE_SDK,
          )
          dependencies(JUNIT4)
        }
      }
      .withAndroidLibProject("features:two") {
        withBuildScript {
          plugins(ANDROID_LIB)
          android = AndroidBlock(
            namespace = "com.example.features.two",
            compileSdkVersion = COMPILE_SDK,
          )
        }
      }
      .withAndroidLibProject("macrobenchmarks") {
        withBuildScript {
          plugins(ANDROID_TEST)
          android = AndroidBlock(
            namespace = "com.example.test",
            compileSdkVersion = COMPILE_SDK,
            targetProjectPath = ":app",
          )
          // validating fallback support
          dependencies(implementation(":features:two"))
        }
      }
      .withSubproject("jvm-kotlin") {
        sources = jvmKotlinSources
        withBuildScript {
          plugins(KOTLIN_JVM)
          dependencies(JUNIT4)
        }
      }
      .withSubproject("jvm-java") {
        sources = jvmJavaSources
        withBuildScript {
          plugins(JAVA_LIB)
          // The only project using JUnit5
          dependencies(JUNIT_PLATFORM, JUNIT_API, JUNIT_ENGINE, JUNIT_LAUNCHER)
          withGroovy("tasks.test { useJUnitPlatform() }")
        }
      }
      .write()
  }

  private val appSources = listOf(
    Source.kotlin(
      """
      package com.example.app
      
      import org.junit.Test

      class One {
        @Test fun test() { assert(true) }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
    Source.kotlin(
      """
      package com.example.app
      
      import org.junit.Test

      class Two {
        @Test fun test() { assert(true) }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
  )

  private val libSources = listOf(
    Source.kotlin(
      """
      package com.example.features.one
      
      import org.junit.Test

      class One {
        @Test fun test() { assert(true) }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
    Source.kotlin(
      """
      package com.example.features.one
      
      import org.junit.Test

      class Two {
        @Test fun test() { assert(true) }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
  )

  private val jvmJavaSources = mutableListOf(
    Source.java(
      """
      package com.example.jvm;
      
      import org.junit.jupiter.api.Test;

      public class One {
        @Test public void test() { assert true; }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
    Source.java(
      """
      package com.example.jvm;
      
      import org.junit.jupiter.api.Test;

      public class Two {
        @Test public void test() { assert true; }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
    Source.java(
      """
      package com.example.jvm;
      
      import org.junit.jupiter.api.Test;

      public class Three {
        @Test public void test() { assert true; }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
  )

  private val jvmKotlinSources = mutableListOf(
    Source.kotlin(
      """
      package com.example.jvm
      
      import org.junit.Test

      class One {
        @Test fun test() { assert(true) }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
    Source.kotlin(
      """
      package com.example.jvm
      
      import org.junit.Test

      class Two {
        @Test fun test() { assert(true) }
      }
      """.trimIndent()
    )
      .withSourceSet("test")
      .build(),
  )

  val expectedTasks = listOf(
    ":app:testDebugUnitTest SKIPPED",
    ":app:test SKIPPED",
    ":features:one:testReleaseUnitTest SKIPPED",
    ":features:one:test SKIPPED",
    ":macrobenchmarks:test SKIPPED",
    ":jvm-java:test SKIPPED",
    ":jvm-kotlin:test SKIPPED",
  )

  val expectedTasksWithExclusions = listOf(
    ":app:testDebugUnitTest SKIPPED",
    ":app:test SKIPPED",
    ":features:one:testDebugUnitTest SKIPPED",
    ":features:one:test SKIPPED",
    ":macrobenchmarks:test SKIPPED",
    ":jvm-java:test SKIPPED",
    ":jvm-kotlin:test SKIPPED",
  )

  fun actualTestResultsForApp(gradleProject: GradleProject): List<String> {
    return actualReportFor(gradleProject, ":app", "reports/tests/testDebugUnitTest")
  }

  fun actualTestResultsForFeaturesOne(gradleProject: GradleProject): List<String> {
    return actualReportFor(gradleProject, ":features:one", "reports/tests/testReleaseUnitTest")
  }

  fun actualTestResultsForJvmJava(gradleProject: GradleProject): List<String> {
    return actualReportFor(gradleProject, ":jvm-java", "reports/tests/test")
  }

  fun actualTestResultsForJvmJavaTwoOptions(gradleProject: GradleProject): List<String> {
    return actualReportFor(gradleProject, ":jvm-java", "reports/tests/test")
  }

  fun actualTestResultsForJvmKotlin(gradleProject: GradleProject): List<String> {
    return actualReportFor(gradleProject, ":jvm-kotlin", "reports/tests/test")
  }

  private fun actualReportFor(gradleProject: GradleProject, projectPath: String, relativePath: String): List<String> {
    val testResults = gradleProject.artifacts(projectPath, relativePath)
    return testResults.asPath
      .listDirectoryEntries()
      .filter { it.isDirectory() }
      .filterNot { it.name == "css" || it.name == "js" }
      .map { it.name }
  }

  val expectedTestResultsForApp = listOf("com.example.app.One")
  val expectedTestResultsForAppNoFilter = listOf("com.example.app.One", "com.example.app.Two")
  val expectedTestResultsForFeaturesOne = listOf("com.example.features.one.One")
  val expectedTestResultsForFeaturesOneNoFilter = listOf("com.example.features.one.One", "com.example.features.one.Two")
  val expectedTestResultsForJvmJava = listOf("com.example.jvm.One")
  val expectedTestResultsForJvmJavaNoFilter = listOf(
    "com.example.jvm.One", "com.example.jvm.Two", "com.example.jvm.Three"
  )
  val expectedTestResultsForJvmJavaTwoOptions = listOf("com.example.jvm.One", "com.example.jvm.Three")
  val expectedTestResultsForJvmKotlin = listOf("com.example.jvm.One")
  val expectedTestResultsForJvmKotlinNoFilter = listOf("com.example.jvm.One", "com.example.jvm.Two")
}
