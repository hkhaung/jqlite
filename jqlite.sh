#!/bin/sh

set -e # Exit early if any commands fail

(
  cd "$(dirname "$0")"
  mvn -B package -Ddir=/tmp/jqlite
)

echo "Running: java -jar /tmp/jqlite/jqlite.jar" "$@"
exec java -jar /tmp/jqlite/jqlite.jar "$@" || echo "Error running the JAR"
