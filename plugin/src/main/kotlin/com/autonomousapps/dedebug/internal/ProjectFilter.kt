// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.internal

import com.autonomousapps.dedebug.extension.AmbiguousIncludesException

internal class ProjectFilter(
  private val projectPath: String,
  private val excludes: Set<String>,
  private val includes: Set<String>,
) {

  fun isExcluded(): Boolean {
    return if (includes.isNotEmpty()) {
      val included = projectPath in includes
      val excluded = projectPath in excludes
      if (included && excluded) {
        throw AmbiguousIncludesException(projectPath, excludes, includes)
      }

      !included
    } else if (excludes.isNotEmpty()) {
      val excluded = projectPath in excludes
      excluded
    } else {
      false
    }
  }
}
