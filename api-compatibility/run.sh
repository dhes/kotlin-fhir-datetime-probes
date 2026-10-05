#!/bin/sh
# Runs the probe with the Gradle wrapper in this folder. Needs a JDK on the PATH or in JAVA_HOME.
HERE="$(cd "$(dirname "$0")" && pwd)"
G="$HERE/gradlew"
VARIANTS="v1 a-field a-field-hidden b-body c-subtype d-equals e-shared"
echo "######## 1. caller compiled once against v1, run against each folder without recompiling"
for v in $VARIANTS; do "$G" -p "$HERE" -q --console=plain ":consumer:runAgainst-${v}" 2>&1 | grep -v "^w: " | cut -c1-140; done
echo
echo "######## 2. the same caller source recompiled against each folder"
for v in $VARIANTS; do
  out=$("$G" -p "$HERE" --console=plain ":recompile-${v}:compileKotlin" --rerun-tasks 2>&1)
  if echo "$out" | grep -q "BUILD SUCCESSFUL"; then echo "${v}: compiles"
  else echo "${v}: DOES NOT COMPILE"; echo "$out" | grep "^e: " | sed -E "s|file://.*/probe/||" | cut -c1-200 | sed 's/^/     /'; fi
done
