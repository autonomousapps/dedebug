# DeDebug Gradle Plugin

[Simplify your Android library development with one line of configuration](https://autonomousapps.com/blog/simplify-your-android-library-development/post/)

## Features

DeDebug eliminates the "debug" build type in every Android library in your build, except those you explicitly opt out. 
It also handles the slightly gnarly configuration so that Gradle variant-matching works transparently.

It also brings Android library modules into feature parity with JVM modules by adding support for the `--tests` CLI option 
when invoking the `test` lifecycle task.

## Installation

**settings.gradle.kts**
```kotlin
plugins {
  id("com.autonomousapps.dedebug") version "0.1"
  id("com.android.library") version "9.3.1" apply false

  // Optional, if you have Kotlin/JVM modules
  id("org.jetbrains.kotlin.jvm") version "2.4.10" apply false

  // Include every plugin here and you simplify your class
  // loading situation.
  ... also *every other plugin* ...
}

dedebug {
  include(...) // optional: only include these modules
  exclude(...) // optional: exclude some modules
}
```

## Usage

The DeDebug Gradle Plugin is essentially transparent to users. Even though it disables the "debug" build type in Android
libraries, it still ensures that common tasks like `testDebugUnitTest` and `connectedDebugAndroidTest` still exist, to
avoid breaking existing workflows and developers' muscle memory.

### Unit tests

DeDebug provides support for the `--tests` CLI option, even when running the "test" lifecycle task.

**Before DeDebug**
```shell
./gradlew android-lib:test --tests AcabTest

FAILURE: Build failed with an exception.

* What went wrong:
Problem configuring task :help from command line.
> Unknown command-line option '--tests'
```

**After DeDebug**
```shell
./gradlew android-lib:test --tests AcabTest

> Task :android-lib:test
Running test: All cats are beautiful()(com.foo.AcabTest)

BUILD SUCCESSFUL
```

### Connected Android tests

Android's connected, or instrumented, tests do not support any standard `--`-style CLI options. See the docs
[here](https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner#filter-tests).
So for these tests, the filtering remains unchanged, e.g.:

```shell
./gradlew android-lib:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class com.foo.AcabTest
```

License
--------

    Copyright 2026 Tony Robalik.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
