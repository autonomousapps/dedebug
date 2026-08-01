// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.extension

public class AmbiguousIncludesException private constructor(msg: String) : RuntimeException(msg) {

  internal constructor(
    projectPath: String,
    excludes: Set<String>,
    includes: Set<String>,
  ) : this(buildMessage(projectPath, excludes, includes))

  internal companion object {
    fun buildMessage(
      projectPath: String,
      excludes: Set<String>,
      includes: Set<String>,
    ): String {
      val includes = includes.joinToString(separator = ", ") { "'$it'" }
      val excludes = excludes.joinToString(separator = ", ") { "'$it'" }

      return $$"""
        Project '$$projectPath' was both included and excluded, which is ambiguous.
          Includes were: $$includes
          Excludes were: $$excludes
        Check '$${DeDebugExtension.NAME}' extension.
      """.trimIndent()
    }
  }
}
