// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.internal

import org.gradle.TaskExecutionRequest
import org.gradle.internal.DefaultTaskExecutionRequest

internal class TaskRequestMapper(private val requests: List<TaskExecutionRequest>) {

  private val requestedTests = mutableListOf<String>()

  fun requestedTests(): List<String> = requestedTests

  fun map(): List<TaskExecutionRequest> {
    var isTests = false

    return requests.map { request ->
      when (request) {
        is DefaultTaskExecutionRequest -> {
          val args = request.args.mapNotNull { arg ->
            when {
              isTests -> {
                isTests = false
                requestedTests.add(arg)
                null // remove this arg
              }

              // Either `--tests TestToRun` (two args) or `--tests=TestToRun` (one arg)
              arg.startsWith("--tests") -> {
                if (arg.startsWith("--tests=")) {
                  // the argument is `--tests=TestToRun`, so the test request is part of THIS arg
                  requestedTests.add(arg.substringAfter("--tests="))
                } else {
                  // parse the next argument as the test request
                  isTests = true
                }

                null // remove this arg
              }

              // Leave non-`DefaultTaskExecutionRequest`s alone.
              else -> arg
            }
          }

          DefaultTaskExecutionRequest.of(args, request.projectPath, request.rootDir)
        }

        else -> request
      }
    }
  }
}
