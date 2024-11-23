#!/bin/sh

# Parameters
NEW_VERSION=$1
REPO="liperium/hdp-app-builds"

echo "1. Test the build"
./gradlew app:test
echo "Ready to build new release? (CTRL+C to quit)"
read

# Update version in build.gradle
echo "2. Gradle clean/sync because of new versionName"
sed -i "s/versionName = \".*\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts
./gradlew clean
./gradlew prepareKotlinBuildScriptModel

echo "3. Build the apks"
./gradlew assembleRelease
echo "Ready to push to github? (CTRL+C to quit)"
read

# Create a new tag and push it
git tag -a $NEW_VERSION -m "Release $NEW_VERSION"
git push origin $NEW_VERSION

# Create a GitHub release
gh release create $NEW_VERSION --repo $REPO --title "Release $NEW_VERSION" --notes "New update" app/build/outputs/apk/release/app-release.apk

# If you need to attach binaries, use the following format:
# gh release create $NEW_VERSION --repo $REPO --title "Release $NEW_VERSION" --notes "Description of the release" path/to/binary
