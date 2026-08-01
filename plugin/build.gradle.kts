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

extra["desc"] = "Disables the debug build type in your Android libraries"
description = extra["desc"].toString()

gradlePlugin {
  plugins.create("com.autonomousapps.dedebug") {
    implementationClass = "com.autonomousapps.dedebug.DeDebugSettingsPlugin"

    displayName = "DeDebug Gradle Plugin"
    description = extra["desc"].toString()
    tags.set(listOf("android"))

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

mavenPublishing {
  publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.VALIDATED)
  signAllPublications()

  // TODO fixup pom
  pom {
    name.set("DeDebug Gradle Plugin")
    description.set(extra["desc"].toString())
    inceptionYear.set("2026")
    //url.set("https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin")
    url.set("TODO")
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }
    developers {
      developer {
        id.set("autonomousapps")
        name.set("Tony Robalik")
        url.set("https://github.com/autonomousapps")
      }
    }
    scm {
      //url.set("https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin")
      //connection.set("scm:git:git://github.com/autonomousapps/dependency-analysis-android-gradle-plugin.git")
      //developerConnection.set("scm:git:ssh://github.com/autonomousapps/dependency-analysis-android-gradle-plugin.git")
    }
  }
}

gradleTestKitSupport {
  withSupportLibrary()
}

tasks {
  withType<Test>().configureEach {
    useJUnitPlatform()
  }
  withType<ValidatePlugins>().configureEach {
    enableStricterValidation.set(true)
  }
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

dependencyAnalysis {
  issues {
    onAny {
      severity("fail")
      exclude(libs.junit.params)
    }
  }
}
