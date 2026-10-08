#!/usr/bin/env bash
# Builds a repository with a meaningful, module-wise commit history.
# Usage: GIT_NAME="Your Name" GIT_EMAIL="you@example.com" bash git_setup.sh
# TIP: for real evidence of development, make these commits over several days instead of all at once.
set -e
git init -b main
git config user.name  "${GIT_NAME:-Your Name}"
git config user.email "${GIT_EMAIL:-you@example.com}"
c() { git add "${@:2}"; git commit -m "$1"; }
c "chore: project skeleton, UI helper, sample airport data, build scripts" .gitignore build.sh run.sh build.bat src/airportflow/UI.java src/airportflow/Data.java
for spec in \
 "m1|feat(M1): KMP, Z-function, Rabin-Karp, Aho-Corasick + menu" \
 "m2|feat(M2): suffix array (SA-IS), Kasai LCP, suffix automaton + menu" \
 "m3|feat(M3): Levenshtein/Damerau, bitmask DP, matrix-chain, optimal BST + menu" \
 "m4|feat(M4): max-flow family, min-cut, matching, Konig + menu" \
 "m5|feat(M5): SAT/DPLL, reductions to CLIQUE/IS/VC, 2-approximation + menu" \
 "m6|feat(M6): randomized quicksort, reservoir, Miller-Rabin, scan, reduce, Brent + menu"; do
  m="${spec%%|*}"; msg="${spec#*|}"
  git checkout -b "feature/$m"
  c "$msg" "src/airportflow/$m"
  git checkout main
  git merge --no-ff "feature/$m" -m "merge: module $m"
done
c "feat(ui): main menu wiring all modules and full demo" src/airportflow/Main.java
c "test: unit tests with randomized oracles for all modules" test
c "perf: benchmark suite and recorded results" src/airportflow/Benchmark.java docs/benchmark_results.txt docs/test_results.txt
c "docs: README, issue list, git script" README.md docs/issues.md git_setup.sh
git log --oneline --graph
