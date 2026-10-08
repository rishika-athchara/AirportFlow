package airportflow.m5;

import java.util.*;

/** Module 5 (CO5): SAT/3-SAT, 3-SAT->CLIQUE->INDEPENDENT-SET->VERTEX-COVER, vertex-cover 2-approximation. */
public final class NPAlgos {
    private NPAlgos() {}

    // ---------------------------------------------------------------- SAT
    /** "1 -2 3, -1 2 3" -> [[1,-2,3],[-1,2,3]] */
    public static List<List<Integer>> parseCnf(String text) {
        List<List<Integer>> cnf = new ArrayList<>();
        for (String c : text.split(",")) {
            if (c.isBlank()) continue;
            List<Integer> cl = new ArrayList<>();
            for (String t : c.trim().split("\\s+")) cl.add(Integer.parseInt(t));
            cnf.add(cl);
        }
        return cnf;
    }

    public static boolean satisfies(List<List<Integer>> cnf, Map<Integer, Boolean> a) {
        for (List<Integer> c : cnf) {
            boolean ok = false;
            for (int l : c) if (a.getOrDefault(Math.abs(l), false) == (l > 0)) { ok = true; break; }
            if (!ok) return false;
        }
        return true;
    }

    /** Exhaustive search, O(2^n * m). */
    public static Map<Integer, Boolean> satBruteForce(List<List<Integer>> cnf) {
        TreeSet<Integer> vs = new TreeSet<>();
        for (List<Integer> c : cnf) for (int l : c) vs.add(Math.abs(l));
        List<Integer> vars = new ArrayList<>(vs);
        for (long bits = 0; bits < (1L << vars.size()); bits++) {
            Map<Integer, Boolean> a = new TreeMap<>();
            for (int i = 0; i < vars.size(); i++) a.put(vars.get(i), (bits >> i & 1) == 1);
            if (satisfies(cnf, a)) return a;
        }
        return null;
    }

    private static List<List<Integer>> simplify(List<List<Integer>> cnf, int lit) {
        List<List<Integer>> res = new ArrayList<>();
        for (List<Integer> c : cnf) {
            if (c.contains(lit)) continue;
            List<Integer> nc = new ArrayList<>(c);
            nc.remove(Integer.valueOf(-lit));
            if (nc.isEmpty()) return null;
            res.add(nc);
        }
        return res;
    }

    /** DPLL with unit propagation. Returns a (possibly partial) satisfying assignment or null. */
    public static Map<Integer, Boolean> dpll(List<List<Integer>> cnf, Map<Integer, Boolean> asg) {
        asg = new TreeMap<>(asg);
        while (true) {
            Integer unit = null;
            for (List<Integer> c : cnf) if (c.size() == 1) { unit = c.get(0); break; }
            if (unit == null) break;
            asg.put(Math.abs(unit), unit > 0);
            cnf = simplify(cnf, unit);
            if (cnf == null) return null;
        }
        if (cnf.isEmpty()) return asg;
        int v = Math.abs(cnf.get(0).get(0));
        for (boolean val : new boolean[]{true, false}) {
            List<List<Integer>> sub = simplify(cnf, val ? v : -v);
            if (sub == null) continue;
            Map<Integer, Boolean> a2 = new TreeMap<>(asg);
            a2.put(v, val);
            Map<Integer, Boolean> r = dpll(sub, a2);
            if (r != null) return r;
        }
        return null;
    }

    // ---------------------------------------------------------------- graphs & reductions
    public static final class Graph {
        public final int n;
        public final boolean[][] adj;
        public Graph(int n) { this.n = n; this.adj = new boolean[n][n]; }
        public void addEdge(int a, int b) { adj[a][b] = adj[b][a] = true; }
        public List<int[]> edges() {
            List<int[]> e = new ArrayList<>();
            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) if (adj[i][j]) e.add(new int[]{i, j});
            return e;
        }
    }

    /** Vertex i is literal litOf[i] of clause clauseOf[i]. */
    public record Reduction(Graph graph, int[] clauseOf, int[] litOf, int k) {}

    /** Edge iff literals come from different clauses and are not contradictory. Satisfiable <=> k-clique, k = #clauses. */
    public static Reduction threeSatToClique(List<List<Integer>> cnf) {
        List<int[]> vs = new ArrayList<>();
        for (int i = 0; i < cnf.size(); i++) for (int l : cnf.get(i)) vs.add(new int[]{i, l});
        Graph g = new Graph(vs.size());
        for (int a = 0; a < vs.size(); a++)
            for (int b = a + 1; b < vs.size(); b++)
                if (vs.get(a)[0] != vs.get(b)[0] && vs.get(a)[1] != -vs.get(b)[1]) g.addEdge(a, b);
        int[] co = new int[vs.size()], lo = new int[vs.size()];
        for (int i = 0; i < vs.size(); i++) { co[i] = vs.get(i)[0]; lo[i] = vs.get(i)[1]; }
        return new Reduction(g, co, lo, cnf.size());
    }

    /** Backtracking search for a clique of size k; null if none. */
    public static List<Integer> findClique(Graph g, int k) {
        List<Integer> all = new ArrayList<>();
        for (int i = 0; i < g.n; i++) all.add(i);
        return extend(g, new ArrayList<>(), all, k);
    }

    private static List<Integer> extend(Graph g, List<Integer> cur, List<Integer> cand, int k) {
        if (cur.size() == k) return cur;
        if (cur.size() + cand.size() < k) return null;
        for (int idx = 0; idx < cand.size(); idx++) {
            int v = cand.get(idx);
            List<Integer> nc = new ArrayList<>();
            for (int j = idx + 1; j < cand.size(); j++) if (g.adj[v][cand.get(j)]) nc.add(cand.get(j));
            List<Integer> next = new ArrayList<>(cur);
            next.add(v);
            List<Integer> r = extend(g, next, nc, k);
            if (r != null) return r;
        }
        return null;
    }

    /** A clique in G is an independent set in the complement of G. */
    public static Graph complement(Graph g) {
        Graph c = new Graph(g.n);
        for (int i = 0; i < g.n; i++) for (int j = i + 1; j < g.n; j++) if (!g.adj[i][j]) c.addEdge(i, j);
        return c;
    }

    public static boolean isIndependentSet(Graph g, Collection<Integer> s) {
        for (int a : s) for (int b : s) if (a != b && g.adj[a][b]) return false;
        return true;
    }

    /** C is a vertex cover iff V \ C is an independent set. */
    public static Set<Integer> independentSetToVertexCover(int n, Collection<Integer> is) {
        Set<Integer> c = new TreeSet<>();
        for (int i = 0; i < n; i++) if (!is.contains(i)) c.add(i);
        return c;
    }

    public static boolean isVertexCover(List<int[]> edges, Collection<Integer> c) {
        for (int[] e : edges) if (!c.contains(e[0]) && !c.contains(e[1])) return false;
        return true;
    }

    // ---------------------------------------------------------------- approximation
    public record Approx(Set<Integer> cover, List<int[]> matching) {}

    /** Both endpoints of a greedy maximal matching: |C| <= 2*OPT. */
    public static Approx vertexCover2Approx(int[][] edges) {
        Set<Integer> cover = new TreeSet<>();
        List<int[]> matching = new ArrayList<>();
        for (int[] e : edges)
            if (!cover.contains(e[0]) && !cover.contains(e[1])) { cover.add(e[0]); cover.add(e[1]); matching.add(e); }
        return new Approx(cover, matching);
    }

    /** Exact minimum vertex cover by subset enumeration (n <= ~22). */
    public static Set<Integer> vertexCoverOptimal(int[][] edges) {
        int n = 0;
        for (int[] e : edges) n = Math.max(n, Math.max(e[0], e[1]) + 1);
        int best = -1;
        for (int mask = 0; mask < (1 << n); mask++) {
            if (best >= 0 && Integer.bitCount(mask) >= Integer.bitCount(best)) continue;
            boolean ok = true;
            for (int[] e : edges) if ((mask >> e[0] & 1) == 0 && (mask >> e[1] & 1) == 0) { ok = false; break; }
            if (ok) best = mask;
        }
        Set<Integer> res = new TreeSet<>();
        for (int i = 0; i < n; i++) if (best >> i >= 0 && (best >> i & 1) == 1) res.add(i);
        return res;
    }
}
