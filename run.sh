#!/usr/bin/env bash
cd "$(dirname "$0")"
[ -d out ] || ./build.sh
case "$1" in
  test)  java -cp out airportflow.AllTests ;;
  bench) java -cp out airportflow.Benchmark ;;
  *)     java -cp out airportflow.Main ;;
esac
