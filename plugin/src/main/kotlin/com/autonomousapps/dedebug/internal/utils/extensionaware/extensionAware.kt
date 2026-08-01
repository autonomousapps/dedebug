// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.internal.utils.extensionaware

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension

internal inline val ExtensionAware.extraProperties: ExtraPropertiesExtension
  get() = extensions.extraProperties

internal inline fun <reified T : Any> ExtraPropertiesExtension.get(name: String): T {
  return if (has(name)) get(name) as T else error("No property named '$name' found!")
}

internal inline fun <reified T : Any> ExtraPropertiesExtension.getOrNull(name: String): T? {
  return if (has(name)) get(name) as T else null
}
