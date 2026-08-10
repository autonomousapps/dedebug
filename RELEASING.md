Release procedure for DeDebug Gradle Plugin

1. Update CHANGELOG.
1. Update README if needed.
1. Bump version number in `plugin/build.gradle.kts` to next stable version _without_ the `-SNAPSHOT` suffix.
1. `git commit -am "chore: prepare for release x.y.z."`.
1. Publish: `./gradlew :plugin:publishEverywhere`.
1. `git tag -a vx.y.z -m "Version x.y.z."`.
1. Update version number in `plugin/build.gradle.kts` to next snapshot version (x.y.z-SNAPSHOT).
1. `git commit -am "chore: prepare next development version."`.
1. `git push && git push --tags`.

nb: if there are ever any issues with publishing to the Gradle Plugin Portal, open an issue on
https://github.com/gradle/plugin-portal-requests/issues and email plugin-portal-support@gradle.com.
