package airportflow;

import airportflow.m1.StringAlgos;
import airportflow.m2.SuffixStructures;
import airportflow.m3.AdvancedDP;
import airportflow.m4.NetworkFlow;
import airportflow.m5.NPAlgos;
import airportflow.m6.RandPar;
import java.math.BigInteger;
import java.util.*;

/** Dependency-free unit tests (no JUnit needed). Run: java -cp out airportflow.AllTests */
public final class AllTests {
    private static int passed = 0, failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name); }
    }

    private static List<Integer> naive(String t, String p) {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i + p.length() <= t.length(); i++) if (t.startsWith(p, i)) r.add(i);
        return r;
    }

    private static String rs(Random r, String alpha, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(alpha.charAt(r.nextInt(alpha.length())));
        return sb.toString();
    }

    public static boolean run() {
        passed = failed = 0;
        Random r = new Random(1);

        System.out.println("-- M1 strings");
        boolean ok = true;
        for (int i = 0; i < 500; i++) {
            String t = rs(r, "ab", r.nextInt(31)), p = rs(r, "ab", 1 + r.nextInt(4));
            List<Integer> e = naive(t, p);
            ok &= StringAlgos.kmp(t, p).equals(e) && StringAlgos.zSearch(t, p).equals(e) && StringAlgos.rabinKarp(t, p).equals(e);
        }
        check("KMP / Z / Rabin-Karp agree with naive on 500 random cases", ok);
        check("KMP overlapping matches", StringAlgos.kmp("aaaa", "aa").equals(List.of(0, 1, 2)));
        check("KMP empty text", StringAlgos.kmp("", "a").isEmpty());
        check("Z-function of aaaaa", Arrays.equals(StringAlgos.zFunction("aaaaa"), new int[]{5, 4, 3, 2, 1}));
        // Aho-Corasick tests removed after module simplification.

        System.out.println("-- M2 suffix structures");
        ok = true;
        for (int i = 0; i < 400; i++) {
            String s = rs(r, "abc", r.nextInt(41));
            Integer[] idx = new Integer[s.length()];
            for (int k = 0; k < idx.length; k++) idx[k] = k;
            Arrays.sort(idx, Comparator.comparing(s::substring));
            int[] exp = new int[idx.length];
            for (int k = 0; k < idx.length; k++) exp[k] = idx[k];
            ok &= Arrays.equals(SuffixStructures.saIs(s), exp) && Arrays.equals(SuffixStructures.saDoubling(s), exp);
        }
        check("SA-IS and doubling equal sorted-suffix oracle on 400 random strings", ok);
        int[] sa = SuffixStructures.saIs("banana");
        check("SA('banana')", Arrays.equals(sa, new int[]{5, 3, 1, 0, 4, 2}));
        check("LCP('banana')", Arrays.equals(SuffixStructures.kasai("banana", sa), new int[]{1, 3, 0, 0, 2}));
        check("longest repeated substring = ana", SuffixStructures.longestRepeated("banana").equals("ana"));
        String ab = "abracadabra";
        check("SA search 'abra' -> [0,7]", SuffixStructures.saSearch(ab, SuffixStructures.saIs(ab), "abra").equals(List.of(0, 7)));
        check("SA search missing pattern", SuffixStructures.saSearch(ab, SuffixStructures.saIs(ab), "zz").isEmpty());
        SuffixStructures.SuffixAutomaton sam = new SuffixStructures.SuffixAutomaton(ab);
        Set<String> subs = new HashSet<>();
        for (int i = 0; i < ab.length(); i++) for (int j = i + 1; j <= ab.length(); j++) subs.add(ab.substring(i, j));
        check("automaton contains / not contains", sam.contains("cada") && !sam.contains("cab"));
        check("automaton distinct substring count", sam.distinctSubstrings() == subs.size());

        System.out.println("-- M3 advanced DP");
        check("Levenshtein kitten/sitting = 3", AdvancedDP.levenshtein("kitten", "sitting") == 3);
        check("Damerau swap CA/AC = 1 (Levenshtein = 2)", AdvancedDP.damerau("CA", "AC") == 1 && AdvancedDP.levenshtein("CA", "AC") == 2);
        check("Damerau 'ca' -> 'abc' = 2 (true DL)", AdvancedDP.damerau("ca", "abc") == 2);
        check("spell-suggest DELYAED -> DELAYED", AdvancedDP.suggest("DELYAED", List.of("DELAYED", "CANCELLED", "BOARDING"), 1).get(0).word().equals("DELAYED"));
        int[][] c = {{9, 2, 7, 8}, {6, 4, 3, 7}, {5, 8, 1, 8}, {7, 6, 9, 4}};
        AdvancedDP.Assignment as = AdvancedDP.bitmaskAssign(c);
        long best = Long.MAX_VALUE;
        int[] perm = {0, 1, 2, 3};
        best = permMin(c, perm, 0, best);
        long sum = 0;
        for (int i = 0; i < 4; i++) sum += c[i][as.gateOfFlight()[i]];
        check("bitmask DP optimal = 13 = brute-force over permutations, assignment consistent", as.cost() == 13 && best == 13 && sum == 13);
        check("matrix chain 10x30x5x60 = 4500", AdvancedDP.matrixChain(new int[]{10, 30, 5, 60}).cost() == 4500);
        check("matrix chain CLRS-style 26000", AdvancedDP.matrixChain(new int[]{40, 20, 30, 10, 30}).cost() == 26000);
        AdvancedDP.Obst ob = AdvancedDP.optimalBst(new long[]{34, 8, 50});
        check("optimal BST cost 142, root = key 'c'", ob.cost() == 142 && AdvancedDP.bstShape(ob.root(), 0, 2, 0).get(0).keyIndex() == 2);

        System.out.println("-- M4 network flow");
        NetworkFlow.FlowNetwork g = new NetworkFlow.FlowNetwork();
        Object[][] es = {{"s", "a", 16}, {"s", "c", 13}, {"a", "c", 10}, {"c", "a", 4}, {"a", "b", 12}, {"c", "d", 14},
                {"b", "c", 9}, {"d", "b", 7}, {"b", "t", 20}, {"d", "t", 4}};
        for (Object[] e : es) g.addEdge((String) e[0], (String) e[1], (Integer) e[2]);
        int f1 = g.fordFulkerson("s", "t"), f2 = g.edmondsKarp("s", "t"), f3 = g.dinic("s", "t");
        check("FF = EK = Dinic = 23 (CLRS network)", f1 == 23 && f2 == 23 && f3 == 23);
        g.edmondsKarp("s", "t");
        check("min-cut capacity equals max-flow", g.minCut("s").stream().mapToInt(NetworkFlow.Cut::capacity).sum() == 23);
        NetworkFlow.FlowNetwork air = new NetworkFlow.FlowNetwork();
        for (String[] e : Data.NETWORK) air.addEdge(e[0], e[1], Integer.parseInt(e[2]));
        check("airport network: algorithms agree (270)", air.dinic("Entrance", "Aircraft") == 270 && air.edmondsKarp("Entrance", "Aircraft") == 270
                && air.fordFulkerson("Entrance", "Aircraft") == 270);
        ok = true;
        for (int it = 0; it < 150; it++) {
            List<String> L = new ArrayList<>();
            Map<String, List<String>> adj = new LinkedHashMap<>();
            for (int i = 0; i < 6; i++) {
                String u = "L" + i;
                L.add(u);
                List<String> nb = new ArrayList<>();
                for (int j = 0; j < 6; j++) if (r.nextDouble() < .3) nb.add("R" + j);
                adj.put(u, nb);
            }
            Map<String, String> m = NetworkFlow.bipartiteMatching(L, adj);
            NetworkFlow.Cover cv = NetworkFlow.konigCover(L, adj, m);
            boolean cover = true;
            for (String u : L) for (String v : adj.get(u)) cover &= cv.left().contains(u) || cv.right().contains(v);
            ok &= cv.size() == m.size() && cover;
        }
        check("Konig: |min vertex cover| = |max matching| and covers all edges (150 random graphs)", ok);

        System.out.println("-- M5 NP-completeness & approximation");
        ok = true;
        for (int it = 0; it < 80; it++) {
            List<List<Integer>> cnf = new ArrayList<>();
            int m = 2 + r.nextInt(4);
            for (int i = 0; i < m; i++) {
                List<Integer> cl = new ArrayList<>();
                List<Integer> vs = new ArrayList<>(List.of(1, 2, 3, 4));
                Collections.shuffle(vs, r);
                for (int k = 0; k < 3; k++) cl.add(vs.get(k) * (r.nextBoolean() ? 1 : -1));
                cnf.add(cl);
            }
            boolean sat = NPAlgos.satBruteForce(cnf) != null;
            NPAlgos.Reduction red = NPAlgos.threeSatToClique(cnf);
            Map<Integer, Boolean> d = NPAlgos.dpll(cnf, new HashMap<>());
            ok &= (NPAlgos.findClique(red.graph(), red.k()) != null) == sat && (d != null) == sat && (d == null || NPAlgos.satisfies(cnf, d));
        }
        check("3-SAT satisfiable <=> k-clique <=> DPLL (80 random formulas vs brute force)", ok);
        List<List<Integer>> f = NPAlgos.parseCnf("1 2 3, -1 -2 3, 1 -2 -3");
        NPAlgos.Reduction red = NPAlgos.threeSatToClique(f);
        List<Integer> cq = NPAlgos.findClique(red.graph(), red.k());
        NPAlgos.Graph comp = NPAlgos.complement(red.graph());
        check("CLIQUE -> INDEPENDENT-SET -> VERTEX-COVER chain valid", cq != null && NPAlgos.isIndependentSet(comp, cq)
                && NPAlgos.isVertexCover(comp.edges(), NPAlgos.independentSetToVertexCover(comp.n, cq)));
        check("unsatisfiable formulas detected", NPAlgos.dpll(NPAlgos.parseCnf("1, -1"), new HashMap<>()) == null
                && NPAlgos.satBruteForce(NPAlgos.parseCnf("1 1 1, -1 -1 -1")) == null);
        ok = true;
        for (int it = 0; it < 100; it++) {
            Set<Long> seen = new HashSet<>();
            List<int[]> el = new ArrayList<>();
            int cnt = 1 + r.nextInt(12);
            for (int k = 0; k < cnt; k++) {
                int a = r.nextInt(8), b = r.nextInt(8);
                if (a == b || !seen.add((long) Math.min(a, b) * 8 + Math.max(a, b))) continue;
                el.add(new int[]{Math.min(a, b), Math.max(a, b)});
            }
            if (el.isEmpty()) continue;
            int[][] E = el.toArray(new int[0][]);
            NPAlgos.Approx ap = NPAlgos.vertexCover2Approx(E);
            Set<Integer> opt = NPAlgos.vertexCoverOptimal(E);
            ok &= NPAlgos.isVertexCover(el, ap.cover()) && NPAlgos.isVertexCover(el, opt) && ap.cover().size() <= 2 * opt.size();
        }
        check("vertex cover 2-approx valid and <= 2*OPT (100 random graphs)", ok);

        System.out.println("-- M6 randomized & parallel");
        int[] a = new int[300];
        for (int i = 0; i < a.length; i++) a[i] = r.nextInt(50);
        int[] sorted = a.clone();
        Arrays.sort(sorted);
        RandPar.quicksort(a, true, r);
        check("randomized quicksort sorts (with duplicates)", Arrays.equals(a, sorted));
        check("quicksort empty / single", RandPar.quicksort(new int[0], true, r) == 0 && RandPar.quicksort(new int[]{1}, true, r) == 0);
        int[] s1 = new int[1000], s2 = new int[1000];
        for (int i = 0; i < 1000; i++) s1[i] = s2[i] = i;
        check("sorted input: deterministic >= 20x randomized comparisons", RandPar.quicksort(s1, false, null) > 20L * RandPar.quicksort(s2, true, new Random(3)));
        int[] cnt = new int[10];
        Random rr = new Random(8);
        for (int it = 0; it < 20000; it++) for (int x : RandPar.reservoir(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).iterator(), 3, rr)) cnt[x]++;
        ok = true;
        for (int x : cnt) ok &= Math.abs(x / 20000.0 - 0.3) < 0.03;
        check("reservoir sampling is uniform (each element ~ k/N = 0.3)", ok);
        boolean[] comp2 = new boolean[5000];
        for (int i = 2; i < 5000; i++) if (!comp2[i]) for (int j = i * i; j < 5000; j += i) comp2[j] = true;
        ok = true;
        for (int n = 0; n < 5000; n++) ok &= RandPar.millerRabin(n) == (n >= 2 && !comp2[n]);
        check("Miller-Rabin equals sieve for n < 5000", ok);
        check("Miller-Rabin: Carmichael 561 composite, 2^61-1 prime", !RandPar.millerRabin(561)
                && RandPar.millerRabin(BigInteger.TWO.pow(61).subtract(BigInteger.ONE), 20, r));
        RandPar.Scan sc = RandPar.blelloch(new long[]{3, 1, 7, 0, 4, 1, 6, 3});
        check("Blelloch scan result, span = 6", Arrays.equals(sc.exclusive(), new long[]{0, 3, 4, 11, 11, 15, 16, 22}) && sc.span() == 6);
        check("Blelloch scan non-power-of-two length", Arrays.equals(RandPar.blelloch(new long[]{5, 2, 9}).exclusive(), new long[]{0, 5, 7}));
        long[] big = new long[101];
        for (int i = 0; i < big.length; i++) big[i] = i;
        try {
            check("thread-pool reduce = 5050", RandPar.parallelReduce(big, 4, Long::sum, 0) == 5050);
        } catch (Exception e) { check("thread-pool reduce", false); }
        check("fork/join sum = 5050", RandPar.forkJoinSum(big) == 5050);
        long[] hund = new long[100];
        for (int i = 0; i < 100; i++) hund[i] = i;
        long[] tr = RandPar.treeReduce(hund);
        check("tree reduce: value 4950, depth 7", tr[0] == 4950 && tr[1] == 7);
        double[] b = RandPar.brent(1000, 10, 4);
        check("Brent bounds W=1000,D=10,p=4 -> (260, 250)", b[0] == 260 && b[1] == 250);

        System.out.printf("%nResult: %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    private static long permMin(int[][] c, int[] p, int k, long best) {
        if (k == p.length) {
            long s = 0;
            for (int i = 0; i < p.length; i++) s += c[i][p[i]];
            return Math.min(best, s);
        }
        for (int i = k; i < p.length; i++) {
            int t = p[k]; p[k] = p[i]; p[i] = t;
            best = permMin(c, p, k + 1, best);
            t = p[k]; p[k] = p[i]; p[i] = t;
        }
        return best;
    }

    public static void main(String[] args) { System.exit(run() ? 0 : 1); }
}
