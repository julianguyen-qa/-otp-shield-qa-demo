#!/bin/sh
# Download gradle-wrapper.jar for Gradle 8.2 so ./gradlew works without running "gradle wrapper".
# Run once: chmod +x get-gradle-wrapper.sh && ./get-gradle-wrapper.sh

set -e
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
JAR_PATH="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"
mkdir -p "$SCRIPT_DIR/gradle/wrapper"

if [ -f "$JAR_PATH" ]; then
  echo "gradle-wrapper.jar already exists."
  exit 0
fi

# From Gradle 8.2 source (same jar used by "gradle wrapper")
URL="https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
echo "Downloading gradle-wrapper.jar..."
if command -v curl >/dev/null 2>&1; then
  curl -sSL -o "$JAR_PATH" "$URL"
elif command -v wget >/dev/null 2>&1; then
  wget -q -O "$JAR_PATH" "$URL"
else
  echo "Need curl or wget to download."
  exit 1
fi

if [ ! -f "$JAR_PATH" ] || [ ! -s "$JAR_PATH" ]; then
  echo "Download failed or empty file. Try manually:"
  echo "  curl -sSL -o gradle/wrapper/gradle-wrapper.jar $URL"
  exit 1
fi
echo "Done. Run: chmod +x gradlew && ./gradlew assembleDebug"
