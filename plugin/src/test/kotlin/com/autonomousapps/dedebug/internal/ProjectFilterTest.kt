// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.internal

import com.autonomousapps.dedebug.extension.AmbiguousIncludesException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ProjectFilterTest {

  @Test
  fun `empty filter - not excluded`() {
    val excludes = emptySet<String>()
    val includes = emptySet<String>()
    val filter = ProjectFilter(
      projectPath = ":project",
      excludes = excludes,
      includes = includes,
    )

    assertThat(filter.isExcluded()).isFalse()
  }

  @Test
  fun `explicitly included - not excluded`() {
    val excludes = emptySet<String>()
    val includes = setOf(":project")
    val filter = ProjectFilter(
      projectPath = ":project",
      excludes = excludes,
      includes = includes,
    )

    assertThat(filter.isExcluded()).isFalse()
  }

  @Test
  fun `not explicitly included - excluded`() {
    val excludes = emptySet<String>()
    val includes = setOf(":other-project")
    val filter = ProjectFilter(
      projectPath = ":project",
      excludes = excludes,
      includes = includes,
    )

    assertThat(filter.isExcluded()).isTrue()
  }

  @Test
  fun `not explicitly excluded - included`() {
    val excludes = setOf(":other-project")
    val includes = emptySet<String>()
    val filter = ProjectFilter(
      projectPath = ":project",
      excludes = excludes,
      includes = includes,
    )

    assertThat(filter.isExcluded()).isFalse()
  }

  @Test
  fun `explicitly excluded - excluded`() {
    val excludes = setOf(":project")
    val includes = emptySet<String>()
    val filter = ProjectFilter(
      projectPath = ":project",
      excludes = excludes,
      includes = includes,
    )

    assertThat(filter.isExcluded()).isTrue()
  }

  @Test
  fun `included and excluded - error`() {
    val excludes = setOf(":project")
    val includes = setOf(":project")
    val filter = ProjectFilter(
      projectPath = ":project",
      excludes = excludes,
      includes = includes,
    )

    val exception = assertThrows<AmbiguousIncludesException> { filter.isExcluded() }
    assertThat(exception.message).isEqualTo(
      """
        Project ':project' was both included and excluded, which is ambiguous.
          Includes were: ':project'
          Excludes were: ':project'
        Check 'dedebug' extension.
      """.trimIndent()
    )
  }
}
