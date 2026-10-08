package airportflow.m5;

import airportflow.Data;
import airportflow.UI;
import java.util.*;

/** Menu for Module 5 - NP-Completeness & Approximation (CO5). */
public final class M5Menu {
    private M5Menu() {}

    public static void run() {
        UI.menu("M5 NP-COMPLETENESS & APPROXIMATION (CO5)", "Back",
                new String[]{"SAT / 3-SAT gate constraints (brute force vs DPLL)", "3-SAT -> CLIQUE reduction",
                        "CLIQUE -> INDEPENDENT-SET -> VERTEX-COVER chain", "Vertex-Cover 2-approximation"},
                new UI.Action[]{M5Menu::sat, M5Menu::reduce, M5Menu::chain, M5Menu::vertexCover});
    }

    private static List<List<Integer>> formula(String def) {
        System.out.println("  Enter 3-CNF: clauses separated by commas, literals by spaces (e.g. 1 -2 3, -1 2 3).");
        System.out.println("  Variable i = 'flight i uses the primary gate'; a negative literal = alternate gate.");
        return NPAlgos.parseCnf(UI.ask("Formula", def));
    }

    public static void sat() {
        List<List<Integer>> f = formula("1 2 3, -1 -2 3, 1 -2 -3, -1 2 -3");
        Map<Integer, Boolean> b = NPAlgos.satBruteForce(f), d = NPAlgos.dpll(f, new HashMap<>());
        System.out.println("  Brute force: " + (b == null ? "UNSATISFIABLE" : b));
        System.out.println("  DPLL       : " + (d == null ? "UNSATISFIABLE" : d + "  (verified: " + NPAlgos.satisfies(f, d) + ")"));
    }

    public static void reduce() {
        List<List<Integer>> f = formula("1 2 3, -1 -2 3, 1 -2 -3");
        NPAlgos.Reduction r = NPAlgos.threeSatToClique(f);
        System.out.println("  3-SAT -> CLIQUE: |V| = " + r.graph().n + ", |E| = " + r.graph().edges().size() + ", target clique size k = " + r.k());
        List<Integer> cl = NPAlgos.findClique(r.graph(), r.k());
        boolean sat = NPAlgos.dpll(f, new HashMap<>()) != null;
        System.out.println("  clique found: " + (cl == null ? "none" : cl) + "   | formula satisfiable (DPLL): " + sat + "   (must agree)");
        if (cl != null) {
            Map<Integer, Boolean> a = new TreeMap<>();
            for (int v : cl) a.put(Math.abs(r.litOf()[v]), r.litOf()[v] > 0);
            System.out.println("  assignment read from the clique (one true literal per clause): " + a);
        }
    }

    public static void chain() {
        List<List<Integer>> f = formula("1 2 3, -1 -2 3, 1 -2 -3");
        NPAlgos.Reduction r = NPAlgos.threeSatToClique(f);
        List<Integer> cl = NPAlgos.findClique(r.graph(), r.k());
        if (cl == null) { System.out.println("  unsatisfiable -> no clique -> no independent set -> no small vertex cover"); return; }
        NPAlgos.Graph comp = NPAlgos.complement(r.graph());
        Set<Integer> vc = NPAlgos.independentSetToVertexCover(comp.n, cl);
        System.out.println("  CLIQUE of size " + r.k() + " -> INDEPENDENT-SET of size " + r.k() + " in complement graph (|E'| = " + comp.edges().size() + ")");
        System.out.println("  independent set valid : " + NPAlgos.isIndependentSet(comp, cl));
        System.out.println("  INDEPENDENT-SET -> VERTEX-COVER of size |V| - k = " + vc.size() + ", valid cover: " + NPAlgos.isVertexCover(comp.edges(), vc));
    }

    public static void vertexCover() {
        int[][] E = Data.TERMINAL_GRAPH;
        System.out.println("  Terminal connection graph edges: " + Arrays.deepToString(E));
        NPAlgos.Approx a = NPAlgos.vertexCover2Approx(E);
        Set<Integer> opt = NPAlgos.vertexCoverOptimal(E);
        System.out.println("  2-approx cover: " + a.cover() + " (size " + a.cover().size() + "), maximal matching: " + Arrays.deepToString(a.matching().toArray()));
        System.out.printf("  Optimal cover : %s (size %d)   ratio = %.2f  (guarantee <= 2)%n", opt, opt.size(), a.cover().size() / (double) opt.size());
        System.out.println("  Staff / cameras on these terminals watch every critical connection.");
    }
}
