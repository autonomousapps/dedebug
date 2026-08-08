// Copyright (c) 2026. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
import com.vanniktech.maven.publish.DeploymentValidation
import org.gradle.plugin.compatibility.compatibility
import tapmoc.Severity

plugins {
  id("java-gradle-plugin")
  id("org.jetbrains.kotlin.jvm")
  id("com.autonomousapps.testkit")
  id("com.gradleup.tapmoc")
  id("com.vanniktech.maven.publish")
  id("com.gradle.plugin-publish")
  id("signing")
}

group = "com.autonomousapps.dedebug"
version = "0.1-SNAPSHOT"

val isSnapshot: Boolean = version.toString().endsWith("SNAPSHOT")
val isRelease: Boolean = !isSnapshot

extra["desc"] = "Disables the debug build type in your Android libraries"
description = extra["desc"] as String

gradlePlugin {
  plugins.create("com.autonomousapps.dedebug") {
    implementationClass = "com.autonomousapps.dedebug.DeDebugSettingsPlugin"

    displayName = "DeDebug Gradle Plugin"
    description = extra["desc"] as String
    tags = listOf("android")

    compatibility {
      features {
        configurationCache = true
      }
    }
  }
}

kotlin {
  explicitApi()
}

tapmoc {
  gradle("9.0.0")
  checkDependencies()
  checkKotlinStdlibs(Severity.ERROR)
}

tasks {
  withType<Test>().configureEach {
    useJUnitPlatform()
  }
  functionalTest {
    addTestListener(object : TestListener {
      override fun beforeTest(testDescriptor: TestDescriptor) {
        logger.lifecycle("Running test: $testDescriptor")
      }
    })
  }
  withType<ValidatePlugins>().configureEach {
    enableStricterValidation = true
  }
}

gradleTestKitSupport {
  withSupportLibrary()
}

dependencies {
  compileOnly(libs.agp.api) {
    because("Consumers should break if they don't manage their classpaths correctly.")
  }

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.assertj)
  testImplementation(libs.junit.api)
  testImplementation(libs.junit.params)

  testRuntimeOnly(libs.junit.engine)
  testRuntimeOnly(libs.junit.launcher)

  functionalTestImplementation(platform(libs.junit.bom))
  functionalTestImplementation(libs.assertj)
  functionalTestImplementation(libs.junit.api)
  functionalTestImplementation(libs.junit.params)

  functionalTestRuntimeOnly(libs.junit.engine)
  functionalTestRuntimeOnly(libs.junit.launcher)
}

mavenPublishing {
  publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.VALIDATED)
  signAllPublications()

  pom {
    name = "DeDebug Gradle Plugin"
    description = extra["desc"] as String
    inceptionYear = "2026"
    url = "https://github.com/autonomousapps/dedebug"
    licenses {
      license {
        name = "The Apache License, Version 2.0"
        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }
    developers {
      developer {
        id = "autonomousapps"
        name = "Tony Robalik"
        url = "https://github.com/autonomousapps"
      }
    }
    scm {
      url = "https://github.com/autonomousapps/dedebug"
      connection = "scm:git:git://github.com/autonomousapps/dedebug.git"
      developerConnection = "scm:git:ssh://github.com/autonomousapps/dedebug.git"
    }
  }
}

val publishToMavenCentral = tasks.named("publishToMavenCentral") {
  configureForRelease()
}

val publishToPluginPortal = tasks.named("publishPlugins") {
  val key = "is-release"
  inputs.property(key, isRelease)
  // Can't publish snapshots to the portal
  onlyIf("only publish releases to the plugin portal") {
    inputs.properties[key] as Boolean
  }

  shouldRunAfter(publishToMavenCentral)
  configureForRelease()
}

tasks.register("publishEverywhere") {
  dependsOn(publishToMavenCentral, publishToPluginPortal)

  group = "publishing"
  description = "Publishes to Plugin Portal and Maven Central"
}

fun Task.configureForRelease() {
  if (isRelease) {
    dependsOn(tasks.check)
  }
}

dependencyAnalysis {
  issues {
    onAny {
      severity("fail")
      exclude(libs.junit.params)
    }
  }
}
