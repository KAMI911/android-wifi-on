# Android build environment
# Usage (via docker-compose):
#   docker-compose run --rm build-debug
#   docker-compose run --rm build-release

FROM eclipse-temurin:17-jdk-jammy

# ── Build arguments (override at build time if needed) ─────────────────────────
ARG CMDLINE_TOOLS_VERSION=11076708
ARG ANDROID_COMPILE_SDK=35
ARG ANDROID_BUILD_TOOLS=35.0.0

# ── Environment ────────────────────────────────────────────────────────────────
ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_USER_HOME=/gradle-cache \
    GRADLE_OPTS="-Dorg.gradle.daemon=false"

ENV PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# ── System packages ─────────────────────────────────────────────────────────────
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        unzip \
        wget \
        git && \
    rm -rf /var/lib/apt/lists/*

# ── Android command-line tools ─────────────────────────────────────────────────
RUN mkdir -p "$ANDROID_HOME/cmdline-tools" && \
    curl -fsSL \
        "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" \
        -o /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-extract && \
    mv /tmp/cmdline-extract/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest" && \
    rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-extract

# ── SDK licenses + components ──────────────────────────────────────────────────
RUN yes | sdkmanager --licenses > /dev/null 2>&1 && \
    sdkmanager \
        "platform-tools" \
        "platforms;android-${ANDROID_COMPILE_SDK}" \
        "build-tools;${ANDROID_BUILD_TOOLS}"

# ── Shared Gradle cache directory ──────────────────────────────────────────────
RUN mkdir -p /gradle-cache && chmod 777 /gradle-cache

# ── Entrypoint ─────────────────────────────────────────────────────────────────
COPY scripts/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR /app

ENTRYPOINT ["/entrypoint.sh"]

# Default task: build debug APK
CMD ["assembleDebug"]
