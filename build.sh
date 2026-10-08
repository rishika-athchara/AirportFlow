#!/usr/bin/env bash
# Compile everything (main + tests) into ./out. Requires JDK 17+ (records, text blocks).
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
if command -v javac >/dev/null 2>&1; then JAVAC=(javac); else JAVAC=(java -m jdk.compiler/com.sun.tools.javac.Main); fi
"${JAVAC[@]}" -d out $(find src test -name '*.java')
echo "Build OK  ->  run: ./run.sh   |  tests: ./run.sh test   |  benchmarks: ./run.sh bench"
