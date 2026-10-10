#!/bin/sh
# Lightweight Gradle bootstrap for Screen-AI.
# Downloads Gradle 8.9 if it is not already available, then runs it.
set -eu
VERSION="8.9"
DIST_URL="https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip"
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/screen-ai-bootstrap"
GRADLE_BIN="$CACHE_DIR/gradle-${VERSION}/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$CACHE_DIR"
  ZIP="$CACHE_DIR/gradle-${VERSION}-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$DIST_URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "$DIST_URL"
  else
    echo "ERROR: curl or wget is required to download Gradle." >&2
    exit 1
  fi
  if command -v unzip >/dev/null 2>&1; then
    unzip -oq "$ZIP" -d "$CACHE_DIR"
  else
    echo "ERROR: unzip is required to extract Gradle." >&2
    exit 1
  fi
fi
exec "$GRADLE_BIN" "$@"
