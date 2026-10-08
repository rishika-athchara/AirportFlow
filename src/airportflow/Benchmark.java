package airportflow;

import airportflow.m1.StringAlgos;
import airportflow.m2.SuffixStructures;
import airportflow.m4.NetworkFlow;
import airportflow.m6.RandPar;
import java.util.*;

/** Performance evaluation (empirical timings and operation counts). */
public final class Benchmark {
    private Benchmark() {}

    private static double ms(Runnable r) { long t = System.nanoTime(); r.run(); return (System.nanoTime() - t) / 1e6; }

    static List<Integer> naive(String t, String p) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i + p.length() <= t.length(); i++) {
            int j = 0;
            while (j < p.length() && t.charAt(i + j) == p.charAt(j)) j++;
            if (j == p.length()) res.add(i);
        }
        return res;
    }

    public static void run() {
        Random r = new Random(42);
        for (int w = 0; w < 3; w++) StringAlgos.kmp("a".repeat(2000) + "b", "a".repeat(50) + "b");   // JIT warm-up
        System.out.println("\n[1] Pattern search, adversarial text 'a'*n+'b', pattern 'a'*500+'b' (ms)");
        System.out.printf("%8s %10s %10s %10s %14s%n", "n", "naive", "KMP", "Z", "Rabin-Karp");
        for (int n : new int[]{20000, 100000, 400000}) {
            String T = "a".repeat(n) + "b", P = "a".repeat(500) + "b";
            System.out.printf("%8d %10.2f %10.2f %10.2f %14.2f%n", n, ms(() -> naive(T, P)), ms(() -> StringAlgos.kmp(T, P)),
                    ms(() -> StringAlgos.zSearch(T, P)), ms(() -> StringAlgos.rabinKarp(T, P)));
        }
        System.out.println("\n[2] Suffix array construction, random string over 'abcd' (ms)");
        System.out.printf("%8s %12s %10s %12s%n", "n", "doubling", "SA-IS", "same result");
        for (int n : new int[]{10000, 100000, 400000}) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('a' + r.nextInt(4)));
            String s = sb.toString();
            int[][] res = new int[2][];
            double a = ms(() -> res[0] = SuffixStructures.saDoubling(s)), b = ms(() -> res[1] = SuffixStructures.saIs(s));
            System.out.printf("%8d %12.2f %10.2f %12b%n", n, a, b, Arrays.equals(res[0], res[1]));
        }
        System.out.println("\n[3] Max-flow on random layered networks (ms) - all three must agree");
        System.out.printf("%6s %6s %10s %10s %10s %7s %6s%n", "V", "E", "FordF", "EdmKarp", "Dinic", "flow", "agree");
        for (int[] cfg : new int[][]{{4, 5}, {6, 10}, {8, 15}, {10, 25}}) {
            NetworkFlow.FlowNetwork g = new NetworkFlow.FlowNetwork();
            int E = 0;
            for (int l = 0; l < cfg[0]; l++)
                for (int i = 0; i < cfg[1]; i++)
                    for (int j = 0; j < cfg[1]; j++)
                        if (r.nextDouble() < .5) { g.addEdge(l + "_" + i, (l + 1) + "_" + j, 1 + r.nextInt(20)); E++; }
            for (int i = 0; i < cfg[1]; i++) { g.addEdge("S", "0_" + i, 50); g.addEdge(cfg[0] + "_" + i, "T", 50); }
            int[] f = new int[3];
            double a = ms(() -> f[0] = g.fordFulkerson("S", "T")), b = ms(() -> f[1] = g.edmondsKarp("S", "T")), c = ms(() -> f[2] = g.dinic("S", "T"));
            System.out.printf("%6d %6d %10.2f %10.2f %10.2f %7d %6b%n", g.nodeCount(), E, a, b, c, f[0], f[0] == f[1] && f[1] == f[2]);
        }
        System.out.println("\n[4] QuickSort comparisons on ALREADY SORTED input");
        System.out.printf("%8s %15s %12s %10s%n", "n", "deterministic", "randomized", "n*log2(n)");
        for (int n : new int[]{500, 1000, 2000, 4000}) {
            int[] a = new int[n], b = new int[n];
            for (int i = 0; i < n; i++) a[i] = b[i] = i;
            System.out.printf("%8d %15d %12d %10d%n", n, RandPar.quicksort(a, false, null), RandPar.quicksort(b, true, new Random(1)),
                    (long) (n * (Math.log(n) / Math.log(2))));
        }
        System.out.println("\n[5] Parallel sum of 50,000,000 longs (ms) - real threads");
        long[] big = new long[50_000_000];
        for (int i = 0; i < big.length; i++) big[i] = i & 1023;
        for (int w = 0; w < 2; w++) Arrays.stream(big).sum();                                                       // warm-up
        long[] out = new long[3];
        double seq = ms(() -> out[0] = Arrays.stream(big).sum());
        System.out.printf("  cores available: %d%n  sequential: %.1f ms%n", Runtime.getRuntime().availableProcessors(), seq);
        for (int p : new int[]{2, 4, 8}) {
            double t = ms(() -> { try { out[1] = RandPar.parallelReduce(big, p, Long::sum, 0); } catch (Exception e) { throw new RuntimeException(e); } });
            System.out.printf("  thread-pool p=%d: %.1f ms  speed-up %.2fx  correct=%b%n", p, t, seq / t, out[1] == out[0]);
        }
        System.out.println("\n[6] Blelloch scan: work/span and Brent bound");
        System.out.printf("%8s %9s %6s %12s %12s%n", "n", "work", "span", "T_4 <=", "T_16 <=");
        for (int n : new int[]{1024, 8192, 65536}) {
            RandPar.Scan s = RandPar.blelloch(new long[n]);
            System.out.printf("%8d %9d %6d %12.0f %12.0f%n", n, s.work(), s.span(), RandPar.brent(s.work(), s.span(), 4)[0], RandPar.brent(s.work(), s.span(), 16)[0]);
        }
    }

    public static void main(String[] args) { run(); }
}
