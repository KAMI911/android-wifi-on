#!/usr/bin/env bash
# Docker entrypoint for Android builds.
# Handles: wrapper jar download, keystore setup, then delegates to ./gradlew.

set -euo pipefail

# ── Gradle wrapper jar ─────────────────────────────────────────────────────────
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "[entrypoint] gradle-wrapper.jar not found — downloading..."
    GRADLE_VER=$(grep distributionUrl gradle/wrapper/gradle-wrapper.properties \
        | sed 's/.*gradle-\([0-9.]*\)-.*/\1/')
    echo "[entrypoint] Gradle version detected: ${GRADLE_VER}"
    curl -fsSL \
        "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VER}/gradle/wrapper/gradle-wrapper.jar" \
        -o gradle/wrapper/gradle-wrapper.jar
    echo "[entrypoint] Wrapper jar downloaded."
fi

chmod +x gradlew

# ── Keystore (release builds) ──────────────────────────────────────────────────
# If KEYSTORE_BASE64 is set and no keystore file exists yet, decode it.
if [ -n "${KEYSTORE_BASE64:-}" ] && [ ! -f "keystore.jks" ]; then
    echo "[entrypoint] Decoding keystore from KEYSTORE_BASE64..."
    echo "$KEYSTORE_BASE64" | base64 --decode > keystore.jks
    export KEYSTORE_FILE="$(pwd)/keystore.jks"
    echo "[entrypoint] KEYSTORE_FILE set to ${KEYSTORE_FILE}"
fi

# ── Version from environment (optional) ────────────────────────────────────────
# Set VERSION_NAME and VERSION_CODE before calling this script to stamp the APK.
# Example: VERSION_NAME=1.2.0 VERSION_CODE=10200 docker-compose run build-release

echo "[entrypoint] Running: ./gradlew $*"
exec ./gradlew "$@"
