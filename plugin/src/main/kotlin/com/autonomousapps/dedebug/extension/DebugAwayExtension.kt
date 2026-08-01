// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.dedebug.extension

import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * By default, DeDebug will remove the debug build type from all Android libraries. If you need to exclude some
 * libraries, you can use the `dedebug` extension in the settings script.
 *
 * If **any** project is listed in `include()`, then **only** projects so listed are included. Anything listed in
 * `exclude()` is excluded. If a project is listed in both `include()` and `exclude()`, that is an error.
 *
 * ```
 * // settings.gradle.kts
 * dedebug {
 *   // Empty (everything is included) by default
 *   include()
 *
 *   // Empty (nothing is excluded) by default
 *   exclude()
 * }
 * ```
 */
public abstract class DeDebugExtension @Inject constructor(objects: ObjectFactory) {

  internal val excludes = objects.setProperty(String::class.java)
  internal val includes = objects.setProperty(String::class.java)

  public fun exclude(vararg excludes: String) {
    exclude(excludes.toList())
  }

  public fun exclude(excludes: List<String>) {
    this.excludes.addAll(excludes)
    this.excludes.disallowChanges()
  }

  public fun include(vararg includes: String) {
    include(includes.toList())
  }

  public fun include(includes: List<String>) {
    this.includes.addAll(includes)
    this.includes.disallowChanges()
  }

  internal companion object {
    const val NAME = "dedebug"

    fun create(settings: Settings): DeDebugExtension {
      return settings.extensions.create(NAME, DeDebugExtension::class.java)
    }
  }
}
