package airportflow;

import airportflow.m1.M1Menu;
import airportflow.m2.M2Menu;
import airportflow.m3.M3Menu;
import airportflow.m4.M4Menu;
import airportflow.m5.M5Menu;
import airportflow.m6.M6Menu;

/**
 * AirportFlow - Airport Operations & Resource Management Platform (DSA-3 project #18).
 * Menu-driven console application.
 */
public final class Main {
    public static void main(String[] args) {
        System.out.println("=".repeat(62));
        System.out.println("  AirportFlow - Airport Operations & Resource Management");
        System.out.println("=".repeat(62));
        UI.menu("MAIN MENU", "Exit",
                new String[]{"View flights & gates", "View operational event log",
                        "M1 - String Algorithms (CO1)", "M2 - Suffix Structures / Document Similarity (CO2)",
                        "M3 - Advanced Dynamic Programming (CO3)", "M4 - Network Flow (CO4)",
                        "M5 - NP-Completeness & Approximation (CO5)", "M6 - Randomized & Parallel Algorithms (CO6)",
                        "Performance benchmarks", "Run unit tests", "Run full non-interactive demo (every module)", "About / syllabus mapping"},
                new UI.Action[]{Main::flights, Main::log, M1Menu::run, M2Menu::run, M3Menu::run, M4Menu::run, M5Menu::run,
                        M6Menu::run, Benchmark::run, Main::tests, Main::fullDemo, Main::about});
        System.out.println("Goodbye!");
    }

    static void flights() {
        System.out.printf("%3s %-7s %-5s %8s %4s %s%n", "ID", "Flight", "Dest", "Dep(min)", "Pax", "Size");
        for (Data.Flight f : Data.FLIGHTS) System.out.printf("%3d %-7s %-5s %8d %4d %c%n", f.id(), f.code(), f.dest(), f.depMin(), f.pax(), f.size());
        System.out.println("\nGates:");
        for (Data.Gate g : Data.GATES) System.out.println("  " + g.name() + " (" + g.size() + ")");
    }

    static void log() {
        for (int i = 0; i < Data.LOG.size(); i++) System.out.printf("  [%02d] %s%n", i, Data.LOG.get(i));
    }

    static void tests() throws Exception {
        try {
            Class.forName("airportflow.AllTests").getMethod("run").invoke(null);
        } catch (ClassNotFoundException e) {
            System.out.println("  AllTests class not on classpath - build with build.sh (it compiles test/ too).");
        }
    }

    static void about() {
        System.out.println("AirportFlow - DSA-3 project: M1 strings | M2 suffix structures | M3 advanced DP | M4 network flow |"
                + " M5 NP & approximation | M6 randomized & parallel");
    }

    static void fullDemo() throws Exception {
        UI.demo = true;
        try {
            Object[][] steps = {
                    {"M1 KMP", (UI.Action) M1Menu::kmp}, {"M1 Z-function", (UI.Action) M1Menu::z},
                    {"M1 Rabin-Karp", (UI.Action) M1Menu::rabinKarp},
                    {"M2 Suffix array (SA-IS)", (UI.Action) M2Menu::index}, {"M2 LCP", (UI.Action) M2Menu::lcp},
                    {"M2 Suffix automaton", (UI.Action) M2Menu::automaton},
                    {"M3 Damerau-Levenshtein", (UI.Action) M3Menu::spell}, {"M3 Bitmask DP", (UI.Action) M3Menu::bitmask},
                    {"M3 Matrix-chain", (UI.Action) M3Menu::matrixChain}, {"M3 Optimal BST", (UI.Action) M3Menu::obst},
                    {"M4 Max flow", (UI.Action) M4Menu::flow}, {"M4 Min-cut", (UI.Action) M4Menu::cut},
                    {"M4 Matching", (UI.Action) M4Menu::matching}, {"M4 Konig", (UI.Action) M4Menu::konig},
                    {"M5 SAT", (UI.Action) M5Menu::sat}, {"M5 3-SAT -> CLIQUE", (UI.Action) M5Menu::reduce},
                    {"M5 CLIQUE -> IS -> VC", (UI.Action) M5Menu::chain}, {"M5 Vertex-cover 2-approx", (UI.Action) M5Menu::vertexCover},
                    {"M6 QuickSort", (UI.Action) M6Menu::quicksort}, {"M6 Reservoir", (UI.Action) M6Menu::reservoir},
                    {"M6 Miller-Rabin", (UI.Action) M6Menu::millerRabin}, {"M6 Blelloch scan", (UI.Action) M6Menu::scan},
                    {"M6 Parallel reduce", (UI.Action) M6Menu::reduce}, {"M6 Brent", (UI.Action) M6Menu::brent}};
            for (Object[] s : steps) { UI.header((String) s[0]); ((UI.Action) s[1]).run(); }
        } finally { UI.demo = false; }
    }
}
