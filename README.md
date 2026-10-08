# AirportFlow - Airport Operations & Resource Management Platform (Java)
**DSA-3 Final Project #18** · Java 17+ (tested on 21) · standard library only · menu-driven console application

## 1. Problem statement, objectives, architecture  *(Rubric 1)*
**Problem.** An airport manages flights and schedules, gates/runways/resources, passenger & baggage information, reports/alerts and a continuous stream of events.
Operators must (a) *search* flight numbers, baggage codes and alert patterns in large logs, (b) *index* operational documents, (c) *correct* human-entered data and
optimise small combinatorial resource choices, (d) *route* passenger flow and *assign* flights to gates, (e) *reason about* hard scheduling constraints, (f) run *real-time
analytics* on streams in parallel.

**Objectives.** Implement every DSA-3 module in one integrated, working system; verify each algorithm with tests; evaluate performance.

**Use cases.** Find all "DELAYED" alerts · find repeated operational text · fix "DELYAED" · choose a gate set · find passenger throughput and its bottleneck · match flights to
size-compatible gates · test gate-constraint satisfiability · sample an event stream · cumulative passenger counts.

### Project layout - one package per syllabus module
```
src/airportflow/
  Main.java            main menu + full demo          UI.java    console helpers      Data.java   sample airport data
  Benchmark.java       performance evaluation
  m1/ StringAlgos.java       M1Menu.java     (CO1)  KMP, Z, Rabin-Karp, Aho-Corasick
  m2/ SuffixStructures.java  M2Menu.java     (CO2)  suffix array (doubling, SA-IS), LCP/Kasai, suffix automaton
  m3/ AdvancedDP.java        M3Menu.java     (CO3)  Levenshtein/Damerau, bitmask DP, matrix-chain, optimal BST
  m4/ NetworkFlow.java       M4Menu.java     (CO4)  Ford-Fulkerson, Edmonds-Karp, Dinic, min-cut, matching, Konig
  m5/ NPAlgos.java           M5Menu.java     (CO5)  SAT/DPLL, 3-SAT->CLIQUE->IS->VC, 2-approximation
  m6/ RandPar.java           M6Menu.java     (CO6)  randomized QuickSort, reservoir, Miller-Rabin, Blelloch, reduce, Brent
test/airportflow/AllTests.java                      42 unit tests (no JUnit required)
```

### DSA-3 syllabus mapping
| Module (CO) | Algorithm | Class | Airport use |
|---|---|---|---|
| M1 (CO1) | KMP | `StringAlgos.kmp` | search flight numbers & alerts |
| | Z-function | `zFunction` / `zSearch` | repeated operational messages |
| | Rabin-Karp | `rabinKarp` | booking & baggage codes |
| | Aho-Corasick | `AhoCorasick` | many flight-status patterns in one pass |
| M2 (CO2) | Suffix array (doubling, **SA-IS**) | `SuffixStructures` | index flight/airport documents |
| | LCP / Kasai | `kasai`, `longestRepeated` | repeated descriptions |
| | Suffix automaton | `SuffixAutomaton` | substring queries |
| M3 (CO3) | Levenshtein / Damerau | `AdvancedDP` | correct airport & passenger-entered data |
| | Bitmask DP | `bitmaskAssign` | small gate/resource combinations |
| | Matrix-chain | `matrixChain` | optimise data-processing sequences |
| | Optimal BST | `optimalBst` | frequently accessed flight information |
| M4 (CO4) | Ford-Fulkerson, Edmonds-Karp, Dinic | `FlowNetwork` | passenger flow, capacity analysis |
| | Bipartite matching | `bipartiteMatching` | flights -> eligible gates |
| | König's theorem, min-cut | `konigCover`, `minCut` | gate-flight conflicts, critical connections |
| M5 (CO5) | SAT / 3-SAT (brute force, DPLL) | `NPAlgos` | gate & scheduling constraints |
| | 3-SAT -> CLIQUE | `threeSatToClique` | computational-analysis reduction |
| | CLIQUE -> INDEPENDENT-SET -> VERTEX-COVER | `complement`, `independentSetToVertexCover` | operational conflicts |
| | Vertex-cover 2-approximation | `vertexCover2Approx` | cover critical connections |
| M6 (CO6) | Randomized QuickSort | `RandPar.quicksort` | rank flight records |
| | Reservoir sampling | `reservoir` | continuous event streams |
| | Miller-Rabin | `millerRabin` | large-number processing |
| | Blelloch scan | `blelloch` | cumulative passenger counts |
| | Parallel reduce (thread pool, fork/join, tree) | `parallelReduce`, `forkJoinSum`, `treeReduce` | transport statistics |
| | Brent's theorem | `brent` | parallel processing bounds |

## 2. Build & run
```bash
./build.sh          # compiles src + test into ./out   (Windows: build.bat)
./run.sh            # interactive menu          = java -cp out airportflow.Main
./run.sh test       # 42 unit tests             (also menu option 10)
./run.sh bench      # performance tables        (also menu option 9)
```
Menu option **11** runs every algorithm non-interactively with default inputs - ideal for a quick demonstration. Each module menu (options 3-8) lets you type your own inputs.
Needs only a JDK 17+ (`javac`); no external libraries.

## 3. Strings & Advanced DP  *(Rubric 2)*
| Algorithm | Time | Space | Why chosen |
|---|---|---|---|
| KMP | O(n+m) | O(m) | guaranteed linear, no hash collisions |
| Z-function | O(n+m) | O(n+m) | detect repeated prefixes in messages |
| Rabin-Karp | O(n+m) expected | O(1) | rolling hash; hits verified, so no false positives |
| Aho-Corasick | O(n+m+z) | O(m·σ) | all status patterns in a single pass |
| SA doubling / **SA-IS** | O(n log² n) / **O(n)** | O(n) | SA-IS for large logs; doubling kept as an oracle |
| Kasai LCP | O(n) | O(n) | longest repeated text |
| Suffix automaton | O(n) | O(n·σ) | online substring queries, distinct-substring count |
| Levenshtein / Damerau | O(nm) | O(m) / O(nm) | Damerau counts a swap ("YA"→"AY") as one edit |
| Bitmask DP | O(2ⁿ·n) | O(2ⁿ) | exact optimum for small n (≤ 10 in the UI) |
| Matrix-chain, Optimal BST | O(n³) | O(n²) | interval DP |

Test cases: empty inputs, overlapping matches, 500 random pattern searches vs a naive oracle, 400 random strings checking SA-IS = doubling = sorted suffixes,
`banana` SA/LCP, textbook DP values (kitten/sitting = 3, matrix-chain 4500 and 26000, OBST cost 142), bitmask DP vs all permutations.

## 4. Network flow, NP-completeness, approximation  *(Rubric 3)*
* **Passenger network** Entrance → Check-in/Kiosk → Security → Immigration/Domestic → Gates → Aircraft: all three algorithms give **270**; the min-cut identifies
  `DomesticGates→Aircraft` and `IntlGates→Aircraft` as the bottleneck. FF O(E·f), EK O(VE²), Dinic O(V²E).
* **Gate assignment:** Kuhn's matching O(VE); König's theorem gives a minimum vertex cover equal in size to the maximum matching (checked on 150 random bipartite graphs).
* **NP-completeness:** 3-SAT → CLIQUE (satisfiable ⇔ clique of size k = #clauses) validated against brute force on 80 random formulas; CLIQUE → INDEPENDENT-SET (complement graph) → VERTEX-COVER (V ∖ IS) chain verified; DPLL solver included.
* **Approximation:** maximal-matching 2-approximation, |C| ≤ 2·OPT (matching edges are disjoint and any cover needs one endpoint of each); verified on 100 random graphs against the exact optimum. The terminal graph reaches ratio 2.0, the worst case.

## 5. Randomized & parallel algorithms, testing, performance  *(Rubric 4)*
Full outputs: `docs/test_results.txt`, `docs/benchmark_results.txt`. Highlights from the recorded run:
* Randomized QuickSort on sorted input, n = 4000: **7,998,000** comparisons (first-pivot) vs **≈ 52,000** (randomized).
* On the 277-node network Dinic (6.7 ms) is ≈ 7× faster than Ford-Fulkerson (50 ms) and ≈ 22× faster than Edmonds-Karp (146 ms); all agree on the flow.
* SA-IS ≈ 8× faster than prefix doubling at n = 400,000, identical output.
* KMP/Rabin-Karp stay linear on the adversarial text where the naive matcher degrades (≈ 530 ms vs ≈ 28 ms at n = 400,000).
* Blelloch scan: work 2(n−1) = O(n), span 2·log₂n = O(log n); Brent bound T_p ≤ W/p + D tabulated for p = 4, 16.
* Reservoir sampling uniformity and Miller-Rabin (vs sieve, Carmichael 561, 2⁶¹−1) are tested.
* **Note on parallel timing:** the recorded run was made on a 1-core machine, so thread-pool speed-up ≈ 1× (overhead only). Re-run `./run.sh bench` on your multi-core laptop and paste your own numbers - expect a clear speed-up for the 50M-element sum.

## 6. GitHub repository & version control  *(Rubric 5)*
```bash
GIT_NAME="Your Name" GIT_EMAIL="you@example.com" bash git_setup.sh   # creates module-wise history on feature branches
git remote add origin https://github.com/<user>/AirportFlow.git
git push -u origin main --all
```
Create GitHub Issues from `docs/issues.md`, commit regularly over several days, and **do not upload everything in one final commit** (the rubric penalises this).

| Member | Modules | Tests |
|---|---|---|
| <Name 1> | M1, M2 (`m1/`, `m2/`) | strings, suffix |
| <Name 2> | M3, M4 (`m3/`, `m4/`) | DP, flow |
| <Name 3> | M5, M6, Main/UI (`m5/`, `m6/`) | NP, randomized/parallel |

## 7. Demonstration & viva guide  *(Rubric 6)*
Demo (≈ 8 min): `./run.sh` → option 11 (full demo) → then interactively: KMP search "DELAYED"; Damerau fix of "DELYAED"; min-cut bottleneck; 3-SAT → CLIQUE; sorted-input QuickSort comparison; Blelloch scan.
Be ready to explain: why SA-IS is linear; why Dinic beats Edmonds-Karp; both directions of the 3-SAT → CLIQUE proof; why the 2-approximation holds; why randomisation defeats adversarial inputs; work vs span and Brent's theorem; and **your own module's code line by line**.
