#!/bin/sh
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$ROOT/.mvn/wrapper/maven-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo 'Downloading Maven Wrapper...'
  URL='https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar'
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$URL" -o "$JAR" || exit 1
  elif command -v wget >/dev/null 2>&1; then wget -q "$URL" -O "$JAR" || exit 1
  else echo 'Install curl or wget to download Maven Wrapper.' >&2; exit 1; fi
fi
if [ -n "$JAVA_HOME" ]; then JAVA="$JAVA_HOME/bin/java"; else JAVA=java; fi
exec "$JAVA" -Dmaven.multiModuleProjectDirectory="$ROOT" -cp "$JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
