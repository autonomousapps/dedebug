// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.internal.utils.string

internal fun String.capitalize(): String {
  return replaceFirstChar(Char::uppercase)
}