// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.internal

import org.assertj.core.api.Assertions.assertThat
import org.gradle.internal.DefaultTaskExecutionRequest
import org.gradle.internal.RunDefaultTasksExecutionRequest
import org.junit.jupiter.api.Test

/**
 * Example task execution request:
 * ```
 * [
 *   DefaultTaskExecutionRequest{
 *     args=[foo:bar:testRUT, --tests, FooBarTest, bar:baz:help],
 *     projectPath='null',
 *     rootDir='null',
 *   }
 * ]
 * ```
 */
internal class TaskRequestMapperTest {

  @Test
  fun `execution requests are unchanged when no tests are requested`() {
    // Given
    val request = DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "--options", "Option", "bar:baz:help"))
    val taskRequests = listOf(request)
    val mapper = TaskRequestMapper(taskRequests)

    // When
    val newRequests = mapper.map()
    val requestedTests = mapper.requestedTests()

    // Then
    assertThat(newRequests).isEqualTo(taskRequests)
    assertThat(requestedTests).isEmpty()
  }

  @Test
  fun `can extract test request`() {
    // Given
    val request = DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "--tests", "FooBarTest", "bar:baz:help"))
    val taskRequests = listOf(request)
    val mapper = TaskRequestMapper(taskRequests)

    // When
    val newRequests = mapper.map()
    val requestedTests = mapper.requestedTests()

    // Then
    assertThat(newRequests).isEqualTo(listOf(DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "bar:baz:help"))))
    assertThat(requestedTests).isEqualTo(listOf("FooBarTest"))
  }

  @Test
  fun `can extract test request with equals`() {
    // Given
    val request = DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "--tests=FooBarTest", "bar:baz:help"))
    val taskRequests = listOf(request)
    val mapper = TaskRequestMapper(taskRequests)

    // When
    val newRequests = mapper.map()
    val requestedTests = mapper.requestedTests()

    // Then
    assertThat(newRequests).isEqualTo(listOf(DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "bar:baz:help"))))
    assertThat(requestedTests).isEqualTo(listOf("FooBarTest"))
  }

  @Test
  fun `can extract multiple test requests`() {
    // Given
    val request =
      DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "--tests", "FooBarTest", "--tests", "BarBazTest"))
    val taskRequests = listOf(request)
    val mapper = TaskRequestMapper(taskRequests)

    // When
    val newRequests = mapper.map()
    val requestedTests = mapper.requestedTests()

    // Then
    assertThat(newRequests).isEqualTo(listOf(DefaultTaskExecutionRequest.of(listOf(":foo:bar:test"))))
    assertThat(requestedTests).isEqualTo(listOf("FooBarTest", "BarBazTest"))
  }

  @Test
  fun `can extract multiple test requests, some with equals`() {
    // Given
    val request = DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "--tests=FooBarTest", "--tests", "BarBazTest"))
    val taskRequests = listOf(request)
    val mapper = TaskRequestMapper(taskRequests)

    // When
    val newRequests = mapper.map()
    val requestedTests = mapper.requestedTests()

    // Then
    assertThat(newRequests).isEqualTo(listOf(DefaultTaskExecutionRequest.of(listOf(":foo:bar:test"))))
    assertThat(requestedTests).isEqualTo(listOf("FooBarTest", "BarBazTest"))
  }

  @Test
  fun `can alter some requests while leaving others alone`() {
    // Given
    val request1 = DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "--tests", "FooBarTest", "bar:baz:help"))
    val request2 = RunDefaultTasksExecutionRequest()
    val taskRequests = listOf(request1, request2)
    val mapper = TaskRequestMapper(taskRequests)

    // When
    val newRequests = mapper.map()
    val requestedTests = mapper.requestedTests()

    // Then
    assertThat(newRequests).isEqualTo(
      listOf(
        DefaultTaskExecutionRequest.of(listOf(":foo:bar:test", "bar:baz:help")),
        request2,
      )
    )
    assertThat(newRequests[1]).isSameAs(request2)
    assertThat(requestedTests).isEqualTo(listOf("FooBarTest"))
  }
}
