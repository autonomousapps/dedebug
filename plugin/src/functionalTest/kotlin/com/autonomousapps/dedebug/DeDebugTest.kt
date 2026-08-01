// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug

import com.autonomousapps.dedebug.fixtures.DeDebugFixture
import com.autonomousapps.kit.GradleBuilder.build
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

internal class DeDebugTest : AbstractFunctionalTest() {

  @Test
  fun `dry-run includes all expected test tasks`() {
    // Given
    val fixture = DeDebugFixture()
    val gradleProject = fixture.build()

    // When
    val result = build(gradleProject.rootDir, "test", "-m")

    // Then
    assertThat(result.output.lines()).containsAll(fixture.expectedTasks)
  }

  @Test
  fun `dry-run includes all expected test tasks when excluding some projects`() {
    // Given
    val fixture = DeDebugFixture(excludes = setOf(":features:one"))
    val gradleProject = fixture.build()

    // When
    val result = build(gradleProject.rootDir, "test", "-m")

    // Then
    assertThat(result.output.lines()).containsAll(fixture.expectedTasksWithExclusions)
  }

  @Test
  fun `can run all tests with no filtering`() {
    // Given
    val fixture = DeDebugFixture()
    val gradleProject = fixture.build()

    // When
    build(gradleProject.rootDir, "test")

    // Then
    assertThat(fixture.actualTestResultsForApp(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForAppNoFilter)
    assertThat(fixture.actualTestResultsForFeaturesOne(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForFeaturesOneNoFilter)
    assertThat(fixture.actualTestResultsForJvmJava(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForJvmJavaNoFilter)
    assertThat(fixture.actualTestResultsForJvmKotlin(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForJvmKotlinNoFilter)
  }

  @Test
  fun `can filter tests with two args`() {
    // Given
    val fixture = DeDebugFixture()
    val gradleProject = fixture.build()

    // When
    build(gradleProject.rootDir, "test", "--tests", "One")

    // Then
    assertThat(fixture.actualTestResultsForApp(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForApp)
    assertThat(fixture.actualTestResultsForFeaturesOne(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForFeaturesOne)
    assertThat(fixture.actualTestResultsForJvmJava(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForJvmJava)
    assertThat(fixture.actualTestResultsForJvmKotlin(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForJvmKotlin)
  }

  @Test
  fun `can filter tests with single arg`() {
    // Given
    val fixture = DeDebugFixture()
    val gradleProject = fixture.build()

    // When
    build(gradleProject.rootDir, "test", "--tests=One")

    // Then
    assertThat(fixture.actualTestResultsForApp(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForApp)
    assertThat(fixture.actualTestResultsForFeaturesOne(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForFeaturesOne)
    assertThat(fixture.actualTestResultsForJvmJava(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForJvmJava)
    assertThat(fixture.actualTestResultsForJvmKotlin(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForJvmKotlin)
  }

  @Test
  fun `can filter tests with multiple options`() {
    // Given
    val fixture = DeDebugFixture()
    val gradleProject = fixture.build()

    // When
    build(gradleProject.rootDir, ":jvm-java:test", "--tests", "One", "--tests", "Three")

    // Then
    assertThat(fixture.actualTestResultsForJvmJavaTwoOptions(gradleProject))
      .containsExactlyInAnyOrderElementsOf(fixture.expectedTestResultsForJvmJavaTwoOptions)
  }

  @Test
  fun `app matchingFallbacks work`() {
    // Given
    val fixture = DeDebugFixture()
    val gradleProject = fixture.build()

    // When
    val result = build(gradleProject.rootDir, ":app:compileDebugKotlin")

    // Then
    assertThat(result.task(":features:one:compileReleaseKotlin")!!.outcome).isEqualTo(TaskOutcome.NO_SOURCE)
  }

  @Test
  fun `test matchingFallbacks work`() {
    // Given
    val fixture = DeDebugFixture()
    val gradleProject = fixture.build()

    // When
    val result = build(gradleProject.rootDir, ":macrobenchmarks:compileDebugKotlin")

    // Then
    assertThat(result.task(":app:compileDebugKotlin")!!.outcome).isEqualTo(TaskOutcome.NO_SOURCE)
    assertThat(result.task(":features:one:compileReleaseKotlin")!!.outcome).isEqualTo(TaskOutcome.NO_SOURCE)
    assertThat(result.task(":features:two:compileReleaseKotlin")!!.outcome).isEqualTo(TaskOutcome.NO_SOURCE)
  }

  // TODO:
  //  1. Handle `androidTestRuntimeOnly(androidx.compose.ui:ui-test-manifest)` requirement for some android tests
}
