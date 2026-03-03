#!/usr/bin/env bash
# Run once after cloning if you are NOT using Android Studio.
# Downloads the Gradle wrapper jar required to run ./gradlew.
#
# Usage:  bash scripts/bootstrap.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
WRAPPER_PROPS="$PROJECT_DIR/gradle/wrapper/gradle-wrapper.properties"
WRAPPER_JAR="$PROJECT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ -f "$WRAPPER_JAR" ]; then
    echo "gradle-wrapper.jar already exists — nothing to do."
    exit 0
fi

if [ ! -f "$WRAPPER_PROPS" ]; then
    echo "ERROR: $WRAPPER_PROPS not found. Are you in the project root?" >&2
    exit 1
fi

GRADLE_VER=$(grep distributionUrl "$WRAPPER_PROPS" | sed 's/.*gradle-\([0-9.]*\)-.*/\1/')
echo "Downloading Gradle ${GRADLE_VER} wrapper jar..."

curl -fsSL \
    "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VER}/gradle/wrapper/gradle-wrapper.jar" \
    -o "$WRAPPER_JAR"

chmod +x "$PROJECT_DIR/gradlew"
echo "Done. You can now run: ./gradlew assembleDebug"
