#!/bin/sh
set -e
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_URL=$(sed -n 's/^wrapperUrl=//p' "$BASE_DIR/.mvn/wrapper/maven-wrapper.properties")
if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$(dirname "$WRAPPER_JAR")"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR";
  elif command -v wget >/dev/null 2>&1; then wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR";
  else echo "curl or wget is required" >&2; exit 1; fi
fi
exec java -classpath "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$BASE_DIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
